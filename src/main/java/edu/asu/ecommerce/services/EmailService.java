package edu.asu.ecommerce.services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {

    private final boolean isDevMode = false;

    private final String senderEmail = "mabanoub427@gmail.com";
    private final String appPassword = "cfvj kgbi ikem cuer"; // No spaces

    public void sendLoginOtp(String recipientEmail, String otpCode) throws Exception {
        if (isDevMode) {
            System.out.println("==========================================");
            System.out.println("[DEV MODE] MOCK EMAIL SENT");
            System.out.println("To: " + recipientEmail);
            System.out.println("Subject: Your Marketplace Login Code");
            System.out.println("OTP Code: " + otpCode);
            System.out.println("==========================================");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // Required for Gmail
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");


        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, appPassword);
            }
        });


        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Your Marketplace Login Code");

            // The email body
            String emailText = "Hello,\n\n"
                    + "Someone is attempting to log into your marketplace account.\n"
                    + "Your One-Time Password (OTP) is: " + otpCode + "\n\n"
                    + "This code will expire in 5 minutes. If this was not you, please secure your account.\n\n"
                    + "Regards,\nMarketplace System";

            message.setText(emailText);

            Transport.send(message);
            System.out.println("Email successfully sent to " + recipientEmail);

        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            throw new Exception("Email delivery failed. Please check server logs.");
        }
    }
}