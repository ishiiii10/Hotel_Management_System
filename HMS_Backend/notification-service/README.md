# Notification Service

## What This Does

This service sends email and SMS notifications when booking events happen. It listens to Kafka events from Booking Service and sends notifications to users.

## Architecture

### Event-Driven
The service listens to these Kafka topics:
- `booking-created` - Sends booking confirmation
- `booking-checked-in` - Sends welcome message
- `booking-completed` - Sends thank you message

### Service Dependencies
- **Auth Service** - Gets user details (email, name, phone) via Feign
- **Hotel Service** - Gets hotel details (name, address) via Feign

## Features

### Email Notifications
- **Booking Confirmation** - Sent when booking is created
- **Check-in Welcome** - Sent when guest checks in
- **Booking Completed** - Thank you message after check-in

### SMS Notifications
Currently it's a mock implementation (just logs the message). It's ready to integrate with actual SMS providers like Twilio or AWS SNS. You can enable it in config.

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

# Email Configuration (Gmail example)
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

### Setting Up Gmail

1. Enable 2-Factor Authentication on your Gmail
2. Generate an App Password: https://myaccount.google.com/apppasswords
3. Use that App Password in `spring.mail.password` (not your regular password)

## Kafka Events

### booking-created
When a booking is created, we send:
- Booking confirmation email
- Booking confirmation SMS (if phone number available)

### booking-checked-in
When guest checks in:
- Welcome email
- Check-in SMS (if phone available)

### booking-completed
After check-in:
- Thank you email

## Service Components

1. **BookingEventListener** - Listens to Kafka topics
2. **NotificationService** - Orchestrates the flow (gets user/hotel details, calls email/SMS services)
3. **EmailService** - Actually sends emails via JavaMailSender
4. **SmsService** - Sends SMS (currently mock, ready for real provider)

## Error Handling

- Email failures are logged but don't break the Kafka consumer
- SMS failures are logged but don't break the flow
- Missing user/hotel data is logged with warnings
- All exceptions are caught so the consumer doesn't crash

## Integration with SMS Providers

The SMS service is currently a mock. To integrate with real providers:

### Twilio Example
You'd need to add Twilio dependency and implement the actual SMS sending.

### AWS SNS Example
Similar, add AWS SDK and implement SMS sending.

## Testing

### Manual Testing
1. Start Kafka, Eureka, Auth Service, Hotel Service, Booking Service
2. Start Notification Service
3. Create a booking via Booking Service
4. Check logs - should see email/SMS sending attempts
5. Check your email inbox for confirmation

## Deployment Notes

1. **Email Config** - Update SMTP settings for production
2. **SMS Provider** - Integrate with actual SMS provider when ready
3. **Error Monitoring** - Set up alerts for notification failures
4. **Rate Limiting** - Consider rate limiting for SMS to avoid costs
5. **Retry Logic** - Could implement retry for failed notifications

## Future Enhancements

- Email templates with HTML
- Real SMS provider integration
- Notification preferences (opt-out)
- Notification history/logging
- Push notifications
- Retry mechanism for failures
- Multi-language support

## Important Notes

- SMS is currently mock (just logs), ready for real integration
- Email needs proper SMTP configuration
- All failures are logged but don't crash the service
- Service fetches user and hotel details from other services via Feign

