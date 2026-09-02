package com.henry.cinnamon.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    /**
     * Sends the 6-digit OTP verification code asynchronously.
     */
    @Async
    public void sendVerificationCode(String toEmail, String verificationCode) {
        // Fallback to console if SMTP is not yet configured
        if (mailSender == null || fromEmail == null || fromEmail.isBlank()) {
            log.warn("⚠SMTP credentials not configured. Displaying OTP in console for {}: {}", toEmail, verificationCode);
            return;
        }

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
            log.info("Verification email sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to {}. Fallback OTP code: {}", toEmail, verificationCode, e);
        }
    }
}
