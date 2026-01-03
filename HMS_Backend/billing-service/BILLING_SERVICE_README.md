# Billing Service Implementation

## Overview
The Billing Service handles bill generation and payment tracking. It listens to booking confirmation events and automatically generates bills.

## Core Responsibilities

1. **Generate Bill**: Automatically creates a bill when a booking is confirmed
2. **Bill Retrieval**: Get bill by booking ID
3. **Mark Bill as Paid**: Admin can mark bills as PAID
4. **Payment Records**: Append-only payment history
5. **User Payments**: Users can view their own payment history
6. **Admin Payments**: Admins can view all payments

## Architecture

### Event-Driven
- Listens to `booking-confirmed` Kafka event
- Automatically generates bill when booking is confirmed

### Database Schema

#### Bill Entity
- `id`: Primary key
- `booking_id`: Unique reference to booking
- `user_id`: User who made the booking
- `hotel_id`, `room_id`: Hotel and room details
- `check_in_date`, `check_out_date`: Booking dates
- `total_amount`: Bill amount
- `status`: PENDING or PAID
- `bill_number`: Unique bill identifier
- `generated_at`: When bill was created
- `paid_at`: When bill was marked as paid

#### Payment Entity (Append-Only)
- `id`: Primary key
- `bill_id`: Reference to bill
- `booking_id`: Reference to booking
- `user_id`: User who made payment
- `amount`: Payment amount
- `payment_method`: CASH, CARD, UPI, etc.
- `transaction_id`: External transaction ID
- `payment_reference`: Payment reference number
- `notes`: Additional notes
- `paid_by`: Admin username who marked as paid
- `paid_at`: Payment timestamp
- `created_at`: Record creation timestamp

## API Endpoints

### 1. Get Bill by Booking ID
```
GET /bills/booking/{bookingId}
Authorization: Bearer <token>
```
**Response:**
```json
{
  "success": true,
  "message": "Bill retrieved successfully",
  "data": {
    "id": 1,
    "bookingId": 4,
    "userId": 28,
    "hotelId": 11,
    "roomId": 7,
    "checkInDate": "2026-01-12",
    "checkOutDate": "2026-01-14",
    "totalAmount": 10000.00,
    "status": "PENDING",
    "billNumber": "BILL-1767433657391",
    "generatedAt": "2026-01-03T16:14:44",
    "paidAt": null,
    "createdAt": "2026-01-03T16:14:44",
    "updatedAt": "2026-01-03T16:14:44"
  }
}
```
**Authorization**: Users can only view their own bills unless ADMIN

### 2. Mark Bill as Paid (Admin Only)
```
POST /bills/{billId}/mark-paid
Authorization: Bearer <token>
```
**Request Body:**
```json
{
  "paymentMethod": "CASH",
  "transactionId": "TXN123456",
  "paymentReference": "REF789",
  "notes": "Payment received at front desk"
}
```
**Response:**
```json
{
  "success": true,
  "message": "Bill marked as paid successfully",
  "data": {
    "id": 1,
    "status": "PAID",
    "paidAt": "2026-01-03T16:20:00",
    ...
  }
}
```
**Authorization**: ADMIN only

### 3. Get My Payments
```
GET /bills/my-payments
Authorization: Bearer <token>
```
**Response:**
```json
{
  "success": true,
  "message": "Payments retrieved successfully",
  "data": [
    {
      "id": 1,
      "billId": 1,
      "bookingId": 4,
      "userId": 28,
      "amount": 10000.00,
      "paymentMethod": "CASH",
      "transactionId": "TXN123456",
      "paidBy": "admin",
      "paidAt": "2026-01-03T16:20:00",
      "createdAt": "2026-01-03T16:20:00"
    }
  ]
}
```

### 4. Get All Payments (Admin Only)
```
GET /bills/payments
Authorization: Bearer <token>
```
**Authorization**: ADMIN only

## Business Flow

### Bill Generation Flow
1. Booking Service confirms a booking
2. Booking Service publishes `booking-confirmed` event
3. Billing Service listens to the event
4. Billing Service generates a bill with status PENDING
5. Bill is stored in database

### Payment Flow
1. Admin marks bill as PAID via API
2. Bill status changes to PAID
3. Payment record is created (append-only)
4. Payment record includes all payment details

## Kafka Events

### booking-confirmed
**Topic**: `booking-confirmed`  
**Payload**:
```json
{
  "bookingId": 4,
  "userId": 28,
  "hotelId": 11,
  "roomId": 7,
  "checkInDate": "2026-01-12",
  "checkOutDate": "2026-01-14",
  "amount": 10000.0,
  "guestEmail": "user@example.com",
  "guestName": "John Doe"
}
```
**Action**: Generates a bill automatically

## Database Setup

Create the database:
```sql
CREATE DATABASE hms_billing_db;
```

## Configuration

### application.properties
```properties
spring.application.name=billing-service
server.port=9005

# DB
spring.datasource.url=jdbc:mysql://localhost:3306/hms_billing_db
spring.datasource.username=root
spring.datasource.password=Ish983556

# Eureka
eureka.client.service-url.defaultZone=http://localhost:8761/eureka

# Kafka Consumer
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=billing-group
```

## Error Handling

- Bill generation failures are logged but don't break the Kafka consumer
- Duplicate bill generation is prevented (checks if bill exists)
- Authorization errors return 403
- Missing bills return 404

## Key Features

1. **Automatic Bill Generation**: Bills are created automatically when bookings are confirmed
2. **Append-Only Payments**: Payment records are immutable (append-only)
3. **Authorization**: Users can only view their own bills/payments
4. **Admin Control**: Only admins can mark bills as paid
5. **Audit Trail**: All payments include who marked them as paid and when

## Testing

1. Create a booking via Booking Service
2. Confirm the booking (or wait for payment service to confirm)
3. Check billing service logs for bill generation
4. Retrieve bill: `GET /bills/booking/{bookingId}`
5. Mark as paid: `POST /bills/{billId}/mark-paid`
6. View payments: `GET /bills/my-payments`

