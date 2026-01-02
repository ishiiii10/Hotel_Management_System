package com.hotelbooking.notification.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${notification.email.from}")
    private String fromEmail;

    @Value("${notification.email.from-name:Hotel Booking System}")
    private String fromName;

    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendBookingConfirmationEmail(String to, String guestName, String hotelName, 
                                           String checkInDate, String checkOutDate, Double amount) {
        String subject = "Booking Confirmation - " + hotelName;
        String body = String.format(
            "Dear %s,\n\n" +
            "Your booking has been confirmed!\n\n" +
            "Hotel: %s\n" +
            "Check-in: %s\n" +
            "Check-out: %s\n" +
            "Total Amount: ₹%.2f\n\n" +
            "Thank you for choosing our hotel!\n\n" +
            "Best regards,\n" +
            "Hotel Booking System",
            guestName, hotelName, checkInDate, checkOutDate, amount
        );
        sendEmail(to, subject, body);
    }

    public void sendCheckInNotificationEmail(String to, String guestName, String hotelName, 
                                            String checkInDate) {
        String subject = "Welcome to " + hotelName;
        String body = String.format(
            "Dear %s,\n\n" +
            "Welcome to %s!\n\n" +
            "Your check-in is confirmed for: %s\n\n" +
            "We hope you have a pleasant stay!\n\n" +
            "Best regards,\n" +
            "Hotel Booking System",
            guestName, hotelName, checkInDate
        );
        sendEmail(to, subject, body);
    }

    public void sendBookingCompletedEmail(String to, String guestName, String hotelName) {
        String subject = "Thank you for staying with us - " + hotelName;
        String body = String.format(
            "Dear %s,\n\n" +
            "Thank you for staying at %s!\n\n" +
            "We hope you had a wonderful experience. We would love to hear your feedback.\n\n" +
            "Looking forward to serving you again!\n\n" +
            "Best regards,\n" +
            "Hotel Booking System",
            guestName, hotelName
        );
        sendEmail(to, subject, body);
    }
}

