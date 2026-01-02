# Notification Service Implementation

## Overview
The Notification Service handles sending email and SMS notifications for booking-related events. It listens to Kafka events from the Booking Service and sends appropriate notifications to users.

## Architecture

### Event-Driven Communication
- Listens to Kafka topics: `booking-created`, `booking-checked-in`, `booking-completed`
- Fetches user and hotel details via Feign clients
- Sends email notifications via SMTP
- Sends SMS notifications (mock implementation, ready for integration)

### Service Dependencies
- **Auth Service**: Fetches user details (email, name, phone)
- **Hotel Service**: Fetches hotel details (name, address, contact)

## Features

### 1. Email Notifications
- **Booking Confirmation**: Sent when a booking is created
- **Check-in Welcome**: Sent when a guest checks in
- **Booking Completed**: Sent after check-in (thank you message)

### 2. SMS Notifications (Mock)
- Currently logs SMS messages
- Ready for integration with SMS providers (Twilio, AWS SNS, etc.)
- Can be enabled via configuration

## Configuration

### application.properties

```properties
# Service Configuration
spring.application.name=notification-service
server.port=9004

# Eureka
eureka.client.service-url.defaultZone=http://localhost:8761/eureka

# Kafka Consumer
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=notification-group
spring.kafka.consumer.auto-offset-reset=earliest

# Email Configuration (Gmail SMTP example)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Email Sender
notification.email.from=noreply@hotelbooking.com
notification.email.from-name=Hotel Booking System

# SMS Configuration
notification.sms.enabled=false
notification.sms.provider=mock
```

### Email Setup (Gmail)
1. Enable 2-Factor Authentication on your Gmail account
2. Generate an App Password: https://myaccount.google.com/apppasswords
3. Use the App Password in `spring.mail.password`

## Kafka Events

### booking-created
**Topic**: `booking-created`  
**Payload**:
```json
{
  "bookingId": 123,
  "userId": 5,
  "hotelId": 1,
  "roomId": 102,
  "checkInDate": "2024-12-25",
  "checkOutDate": "2024-12-30",
  "amount": 5000.0
}
```
**Actions**:
- Sends booking confirmation email
- Sends booking confirmation SMS (if phone available)

### booking-checked-in
**Topic**: `booking-checked-in`  
**Payload**:
```json
{
  "bookingId": 123,
  "userId": 5,
  "hotelId": 1
}
```
**Actions**:
- Sends welcome email
- Sends check-in SMS (if phone available)

### booking-completed
**Topic**: `booking-completed`  
**Payload**:
```json
{
  "bookingId": 123,
  "userId": 5,
  "hotelId": 1
}
```
**Actions**:
- Sends thank you email

## API Endpoints

### Internal Endpoints (for Feign clients)

#### Auth Service
- `GET /internal/auth/users/{userId}` - Get user information

#### Hotel Service
- `GET /internal/hotels/{hotelId}` - Get hotel information

## Service Components

### 1. BookingEventListener
Listens to Kafka topics and delegates to NotificationService.

### 2. NotificationService
Orchestrates the notification flow:
- Fetches user details from Auth Service
- Fetches hotel details from Hotel Service
- Calls EmailService and SmsService

### 3. EmailService
Handles email sending via JavaMailSender:
- `sendBookingConfirmationEmail()`
- `sendCheckInNotificationEmail()`
- `sendBookingCompletedEmail()`

### 4. SmsService
Handles SMS sending (mock implementation):
- `sendBookingConfirmationSms()`
- `sendCheckInNotificationSms()`
- Ready for integration with actual SMS providers

## Error Handling

- Email failures are logged but don't break the Kafka consumer
- SMS failures are logged but don't break the flow
- Missing user/hotel data is logged with warnings
- All exceptions are caught and logged to prevent consumer failures

## Integration with SMS Providers

### Twilio Integration Example
```java
@Service
public class TwilioSmsService {
    private final TwilioRestClient client;
    
    public void sendSms(String to, String message) {
        Message.creator(
            new PhoneNumber(to),
            new PhoneNumber("+1234567890"), // Your Twilio number
            message
        ).create();
    }
}
```

### AWS SNS Integration Example
```java
@Service
public class AwsSnsService {
    private final AmazonSNS snsClient;
    
    public void sendSms(String phoneNumber, String message) {
        PublishRequest request = new PublishRequest()
            .withPhoneNumber(phoneNumber)
            .withMessage(message);
        snsClient.publish(request);
    }
}
```

## Testing

### Manual Testing
1. Start Kafka, Eureka, Auth Service, Hotel Service, Booking Service
2. Start Notification Service
3. Create a booking via Booking Service
4. Check logs for email/SMS sending
5. Check email inbox for confirmation

### Unit Testing
```java
@SpringBootTest
class NotificationServiceTest {
    @MockBean
    private EmailService emailService;
    
    @Test
    void testBookingCreatedNotification() {
        // Test notification sending
    }
}
```

## Deployment Notes

1. **Email Configuration**: Update SMTP settings for production
2. **SMS Provider**: Integrate with actual SMS provider
3. **Error Monitoring**: Set up alerts for notification failures
4. **Rate Limiting**: Consider rate limiting for SMS to avoid costs
5. **Retry Logic**: Implement retry logic for failed notifications

## Future Enhancements

- [ ] Email templates (HTML)
- [ ] SMS provider integration (Twilio/AWS SNS)
- [ ] Notification preferences (user can opt-out)
- [ ] Notification history/logging
- [ ] Push notifications
- [ ] Retry mechanism for failed notifications
- [ ] Rate limiting
- [ ] Multi-language support

