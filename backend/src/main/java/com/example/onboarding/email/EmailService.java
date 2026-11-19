
package com.example.onboarding.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String recipientEmail, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderEmail);
        message.setTo(recipientEmail);
        message.setSubject("KCB Customer Onboarding - Email Verification");

        message.setText(
                "Dear Applicant,\n\n" +
                        "Your KCB Customer Onboarding verification code is:\n\n" +
                        otp + "\n\n" +
                        "This code expires in 5 minutes.\n\n" +
                        "Do not share this code with anyone.\n\n" +
                        "Thank you,\n" +
                        "KCB Customer Onboarding Team"
        );

        mailSender.send(message);
    }
}
