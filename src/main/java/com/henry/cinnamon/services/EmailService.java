package com.henry.cinnamon.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${resend.api-key:${RESEND_API_KEY:}}")
    private String resendApiKey;

    @Value("${resend.from-email:${RESEND_FROM_EMAIL:Cinnamon <onboarding@resend.dev>}}")
    private String resendFromEmail;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Sends the 6-digit OTP verification code asynchronously.
     * Tries Resend HTTPS API (port 443) first, then falls back to SMTP, then to console.
     */
    @Async
    public void sendVerificationCode(String toEmail, String verificationCode) {
        // 1. Primary for Cloud / Render: Resend HTTPS API (Port 443 is never blocked by cloud firewalls)
        if (resendApiKey != null && !resendApiKey.isBlank()) {
            if (sendViaResendApi(toEmail, verificationCode)) {
                return;
            }
            log.warn("Resend dispatch failed. Attempting SMTP fallback for {}...", toEmail);
        }

        // 2. Secondary: Standard JavaMail SMTP (e.g. for local dev or paid hosting where port 587 is unblocked)
        if (mailSender != null && fromEmail != null && !fromEmail.isBlank()) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(toEmail);
                message.setSubject("Verify your Cinnamon Account");
                message.setText(String.format("""
                    Welcome to Cinnamon!

                    Your account verification code is:

                    %s

                    This code will expire in 24 hours. Once verified, your daily request limit will be upgraded from 50 to 1,000 requests/day.

                    If you did not request this, please ignore this email.

                    Happy coding,
                    The Cinnamon Team
                    """, verificationCode));

                mailSender.send(message);
                log.info("Verification email sent successfully via SMTP to {}", toEmail);
                return;
            } catch (Exception e) {
                log.error("SMTP sending failed for {}. Error: {}", toEmail, e.getMessage());
            }
        }

        // 3. Fallback: Log OTP in console
        log.warn("⚠No active email provider succeeded. Fallback OTP code for {}: {}", toEmail, verificationCode);
    }

    private boolean sendViaResendApi(String toEmail, String verificationCode) {
        try {
            String from = (resendFromEmail != null && !resendFromEmail.isBlank())
                    ? resendFromEmail.trim()
                    : "Cinnamon <onboarding@resend.dev>";

            String bodyText = String.format(
                    "Welcome to Cinnamon!\\n\\nYour account verification code is:\\n\\n%s\\n\\nThis code will expire in 24 hours. Once verified, your daily request limit will be upgraded from 50 to 1,000 requests/day.\\n\\nHappy coding,\\nThe Cinnamon Team",
                    verificationCode
            );

            String jsonPayload = String.format("""
                {
                    "from": "%s",
                    "to": ["%s"],
                    "subject": "Verify your Cinnamon Account",
                    "text": "%s"
                }
                """, escapeJson(from), escapeJson(toEmail), bodyText);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Bearer " + resendApiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Verification email sent successfully via Resend HTTPS API to {}. Response: {}", toEmail, response.body());
                return true;
            } else {
                log.error("Resend API error HTTP {}: {}", response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            log.error("Exception during Resend API dispatch for {}: {}", toEmail, e.getMessage(), e);
            return false;
        }
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
