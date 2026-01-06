# Billing Service

## Overview

This service handles bill generation and payment tracking. It listens to booking events and automatically creates bills. When a booking is created or confirmed, a bill gets generated automatically.

## What It Does

1. **Generate Bills** - Automatically creates bills when bookings are created/confirmed
2. **Get Bills** - Retrieve bills by booking ID
3. **Mark as Paid** - Staff can mark bills as PAID
4. **Payment Records** - Keeps track of all payments (append-only, can't be deleted)
5. **View Payments** - Users can see their payment history, admins can see all

## Architecture

### Event-Driven
The service listens to Kafka events:
- `booking-created` - Generates bill immediately for all bookings (PUBLIC and WALK_IN)
- `booking-confirmed` - Also generates bill (for PUBLIC bookings that get confirmed later)

### Database Tables

#### Bills Table
- `id` - Primary key
- `booking_id` - Links to booking (unique)
- `user_id` - Who made the booking
- `hotel_id`, `room_id` - Hotel and room info
- `check_in_date`, `check_out_date` - Dates
- `total_amount` - How much the bill is for
- `status` - PENDING or PAID
- `bill_number` - Unique bill number
- `generated_at` - When bill was created
- `paid_at` - When it was marked as paid

#### Payments Table (Append-Only)
- `id` - Primary key
- `bill_id` - Links to bill
- `booking_id` - Links to booking
- `user_id` - Who paid
- `amount` - Payment amount
- `payment_method` - CASH, CARD, UPI, etc.
- `transaction_id` - External transaction ID (optional)
- `payment_reference` - Payment reference (optional)
- `notes` - Any notes
- `paid_by` - Who marked it as paid (admin username)
- `paid_at` - When payment was recorded
- `created_at` - When record was created

## API Endpoints

### Get Bill by Booking ID
```
GET /bills/booking/{bookingId}
```
Need to be logged in. 
- ADMIN can view any bill
- MANAGER/RECEPTIONIST can view bills for their hotel
- GUEST can only view their own bills

If bill doesn't exist for a CREATED booking, it will auto-generate it.

### Manually Generate Bill (Admin Only)
```
POST /bills/generate/{bookingId}
```
Use this if the Kafka event was missed or something went wrong. Only admins can do this.

### Mark Bill as Paid
```
POST /bills/{billId}/mark-paid
```
- ADMIN, RECEPTIONIST, MANAGER can mark any bill as paid
- GUEST can mark their own bills as paid

When a bill is marked as paid:
- Bill status changes to PAID
- Payment record is created (append-only)
- If booking status is CREATED, it automatically confirms the booking

### Get My Payments
```
GET /bills/my-payments
```
Returns all payment records for the logged-in user.

### Get All Payments (Admin Only)
```
GET /bills/payments
```
Only admins can see all payments.

## How Bill Generation Works

1. Booking Service creates a booking
2. Booking Service publishes `booking-created` Kafka event
3. Billing Service listens to the event
4. Billing Service generates a bill with status PENDING
5. Bill is saved to database

For PUBLIC bookings that need confirmation:
1. Booking gets confirmed
2. `booking-confirmed` event is published
3. Bill gets generated (if not already exists)

## How Payment Works

1. Staff or guest marks bill as PAID via API
2. Bill status changes to PAID
3. Payment record is created (can't be deleted, append-only)
4. If booking was CREATED status, it automatically gets confirmed

## Database Setup

```sql
CREATE DATABASE hms_billing_db;
```

## Configuration

```properties
spring.application.name=billing-service
server.port=9005

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/hms_billing_db
spring.datasource.username=root
spring.datasource.password=Ish983556

# Eureka
eureka.client.service-url.defaultZone=http://localhost:8761/eureka

# Kafka Consumer
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=billing-group
```

## Key Features

1. **Automatic Bill Generation** - Bills are created automatically via Kafka events
2. **Append-Only Payments** - Payment records can't be deleted (audit trail)
3. **Authorization** - Users can only see their own bills/payments
4. **Auto-Confirm Booking** - When bill is paid, CREATED bookings get confirmed automatically
5. **Manual Generation** - Admins can manually generate bills if needed

## Integration Points

- **Booking Service** - Listens to booking events via Kafka
- **Booking Service** - Can confirm bookings when bills are paid (via Feign)

## Error Handling

- Bill generation failures are logged but don't break Kafka consumer
- Duplicate bills are prevented (checks if bill already exists)
- Authorization errors return 403
- Missing bills return 404

## Testing

1. Create a booking via Booking Service
2. Check billing service logs - should see bill generation
3. Get bill: `GET /bills/booking/{bookingId}`
4. Mark as paid: `POST /bills/{billId}/mark-paid`
5. View payments: `GET /bills/my-payments`

## Important Notes

- Bills are generated automatically, you usually don't need to create them manually
- Payment records are append-only for audit purposes
- When bill is paid, CREATED bookings get confirmed automatically
- Walk-in bookings get bills generated immediately when created

