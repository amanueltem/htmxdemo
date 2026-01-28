package com.aman.htmxdemo.user;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Async // Runs on a Virtual Thread if configured in Spring Boot 4
    public void sendPasswordResetEmail(String to, String token) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        // Update this URL based on your production domain
        String resetUrl = "http://localhost:8080/forgot-password/reset?token=" + token;

        String htmlContent = String.format("""
            <h3>Password Reset Request</h3>
            <p>You requested a password reset for FinanceManager-HTMX.</p>
            <p>Click the link below to reset your password. This link expires in 5 minutes.</p>
            <a href="%s">Reset Password</a>
            """, resetUrl);

        helper.setText(htmlContent, true);
        helper.setTo(to);
        helper.setSubject("Reset Your Password");

        mailSender.send(mimeMessage);
    }
}