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

    // Brevo HTTPS API configuration
    @Value("${brevo.api-key:${BREVO_API_KEY:}}")
    private String brevoApiKey;

    @Value("${brevo.sender-email:${BREVO_SENDER_EMAIL:test.experiment.404@gmail.com}}")
    private String brevoSenderEmail;

    @Value("${brevo.sender-name:${BREVO_SENDER_NAME:Cinnamon}}")
    private String brevoSenderName;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Sends the 6-digit OTP verification code asynchronously.
     * Order of precedence:
     * 1. Brevo HTTPS API (port 443, sends to ANY recipient with free tier)
     * 2. Standard JavaMail SMTP (port 587, local dev fallback)
     * 3. Console log fallback
     */
    @Async
    public void sendVerificationCode(String toEmail, String verificationCode) {
        // 1. Primary: Brevo HTTPS API (Port 443, sends to any recipient)
        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            if (sendViaBrevoApi(toEmail, verificationCode)) {
                return;
            }
            log.warn("Brevo dispatch failed for {}. Trying SMTP fallback...", toEmail);
        }

        // 2. Secondary: Standard JavaMail SMTP
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
        log.warn("⚠No email provider succeeded. Fallback OTP code for {}: {}", toEmail, verificationCode);
    }

    private boolean sendViaBrevoApi(String toEmail, String verificationCode) {
        try {
            String senderEmail = (brevoSenderEmail != null && !brevoSenderEmail.isBlank())
                    ? brevoSenderEmail.trim()
                    : "test.experiment.404@gmail.com";
            String senderName = (brevoSenderName != null && !brevoSenderName.isBlank())
                    ? brevoSenderName.trim()
                    : "Cinnamon";

            String textContent = String.format(
                    "Welcome to Cinnamon!\n\nYour account verification code is:\n\n%s\n\nThis code will expire in 24 hours. Once verified, your daily request limit will be upgraded from 50 to 1,000 requests/day.\n\nHappy coding,\nThe Cinnamon Team",
                    verificationCode
            );

            String htmlContent = String.format(
                    "<div style='font-family: monospace; padding: 24px; background-color: #FAF6EE; color: #171512; border: 1px solid #171512; max-width: 520px;'>"
                    + "<div style='display: flex; align-items: center; margin-bottom: 12px;'>"
                    + "<span style='color: #E0447D; font-weight: bold; font-size: 11px; letter-spacing: 2px;'>CINNAMON · ACCESS VERIFICATION</span>"
                    + "</div>"
                    + "<h2 style='font-size: 20px; color: #171512; margin-top: 0;'>Your Developer OTP Code</h2>"
                    + "<p style='color: #6F6A5B; font-size: 13px; line-height: 1.5;'>Enter this 6-digit code in your Cinnamon Developer portal to unlock 1,000 requests/day:</p>"
                    + "<div style='font-size: 32px; font-weight: bold; letter-spacing: 6px; padding: 14px 28px; background: #171512; color: #0D8D9C; display: inline-block; margin: 16px 0;'>%s</div>"
                    + "<p style='color: #6F6A5B; font-size: 11px; margin-top: 16px;'>This code will expire in 24 hours. If you did not request this, please ignore this email.</p>"
                    + "<hr style='border: none; border-top: 0.8px solid #171512; opacity: 0.2; margin: 20px 0;' />"
                    + "<span style='font-size: 10px; color: #6F6A5B; letter-spacing: 1px;'>CINNAMON · 2026</span>"
                    + "</div>",
                    verificationCode
            );

            String jsonPayload = String.format("""
                {
                    "sender": {
                        "name": "%s",
                        "email": "%s"
                    },
                    "to": [
                        {
                            "email": "%s"
                        }
                    ],
                    "subject": "Your Cinnamon Verification Code: %s",
                    "textContent": "%s",
                    "htmlContent": "%s"
                }
                """,
                escapeJson(senderName),
                escapeJson(senderEmail),
                escapeJson(toEmail),
                verificationCode,
                escapeJson(textContent),
                escapeJson(htmlContent)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .timeout(Duration.ofSeconds(15))
                    .header("api-key", brevoApiKey.trim())
                    .header("Content-Type", "application/json")
                    .header("accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Verification email sent successfully via Brevo HTTPS API to {}. Response: {}", toEmail, response.body());
                return true;
            } else {
                log.error("Brevo API error HTTP {}: {}", response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            log.error("Exception during Brevo API dispatch for {}: {}", toEmail, e.getMessage(), e);
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
