# Booking & Billing Flow - Testing Guide

## Complete Flow Overview

```
1. CREATE BOOKING (status: CREATED)
   ↓
2. CONFIRM BOOKING (status: CONFIRMED) ← Payment Service would do this
   ↓ (publishes booking-confirmed event)
3. BILL GENERATED (status: PENDING)
   ↓
4. CHECK-IN (status: CHECKED_IN) ← Only CONFIRMED bookings can check-in
   ↓
5. CHECK-OUT (status: CHECKED_OUT) ← Releases room, publishes checkout-completed event
   ↓
6. MARK BILL AS PAID (status: PAID) ← Admin marks bill as paid
```

## Step-by-Step Testing in Postman

### Step 1: Create a Booking

**Endpoint:** `POST /api/bookings`

**Headers:**
```
Authorization: Bearer <your-jwt-token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "hotelId": 1,
  "roomId": 1,
  "checkInDate": "2026-01-15",
  "checkOutDate": "2026-01-17",
  "numberOfGuests": 2
}
```

**Response:**
- Booking created with `status: "CREATED"`
- `booking-created` event published (notification service receives it)
- **Note:** No bill generated yet!

---

### Step 2: Confirm the Booking (Simulates Payment)

**Endpoint:** `POST /api/bookings/{bookingId}/confirm`

**Headers:**
```
Authorization: Bearer <admin-or-manager-jwt-token>
Content-Type: application/json
```

**Example:**
```
POST /api/bookings/4/confirm
```

**Response:**
- Booking status changes to `"CONFIRMED"`
- `booking-confirmed` event published
- **Billing Service automatically generates a bill** (status: PENDING)
- Notification Service sends confirmation email

**Important:** This endpoint is for testing. In production, Payment Service would call `confirmBooking()` internally after successful payment.

---

### Step 3: Verify Bill Generation

**Endpoint:** `GET /api/bills/booking/{bookingId}`

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Example:**
```
GET /api/bills/booking/4
```

**Response:**
```json
{
  "success": true,
  "message": "Bill retrieved successfully",
  "data": {
    "id": 1,
    "bookingId": 4,
    "status": "PENDING",
    "totalAmount": 10000.00,
    "billNumber": "BILL-1767433657391",
    ...
  }
}
```

---

### Step 4: Check-In Guest (Optional - for complete flow)

**Endpoint:** `POST /api/bookings/{bookingId}/check-in`

**Headers:**
```
Authorization: Bearer <staff-jwt-token>
X-Hotel-Id: <hotelId>
Content-Type: application/json
```

**Request Body (optional):**
```json
{
  "notes": "Guest arrived early"
}
```

**Response:**
- Booking status changes to `"CHECKED_IN"`
- `guest-checked-in` event published
- Notification Service sends welcome email

**Note:** Only `CONFIRMED` bookings can be checked in!

---

### Step 5: Check-Out Guest (Optional - for complete flow)

**Endpoint:** `POST /api/bookings/{bookingId}/check-out`

**Headers:**
```
Authorization: Bearer <staff-jwt-token>
X-Hotel-Id: <hotelId>
Content-Type: application/json
```

**Request Body (optional):**
```json
{
  "notes": "Guest checked out on time"
}
```

**Response:**
- Booking status changes to `"CHECKED_OUT"`
- Room released back to availability
- `checkout-completed` event published
- Notification Service sends thank-you and feedback emails

---

### Step 6: Mark Bill as Paid

**Endpoint:** `POST /api/bills/{billId}/mark-paid`

**Headers:**
```
Authorization: Bearer <admin-jwt-token>
Content-Type: application/json
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
- Bill status changes to `"PAID"`
- Payment record created (append-only)
- `paidAt` timestamp set

---

## Quick Test Sequence

### Minimal Flow (Just to Generate Bill):

1. **Create Booking:**
   ```
   POST /api/bookings
   Body: { "hotelId": 1, "roomId": 1, "checkInDate": "2026-01-15", "checkOutDate": "2026-01-17", "numberOfGuests": 2 }
   ```
   → Returns booking with `id` (e.g., `bookingId = 4`)

2. **Confirm Booking:**
   ```
   POST /api/bookings/4/confirm
   ```
   → Bill automatically generated!

3. **Get Bill:**
   ```
   GET /api/bills/booking/4
   ```
   → Returns bill with `id` (e.g., `billId = 1`)

4. **Mark Bill as Paid:**
   ```
   POST /api/bills/1/mark-paid
   Body: { "paymentMethod": "CASH", "transactionId": "TXN123" }
   ```
   → Bill marked as PAID, payment record created

---

## Event Flow

### When Booking is Created:
- ✅ `booking-created` event published
- ✅ Notification Service: Schedules check-in reminder (24h before)

### When Booking is Confirmed:
- ✅ `booking-confirmed` event published
- ✅ **Billing Service: Generates bill (PENDING)**
- ✅ Notification Service: Sends confirmation email

### When Guest Checks In:
- ✅ `guest-checked-in` event published
- ✅ Notification Service: Sends welcome email, cancels reminder

### When Guest Checks Out:
- ✅ `checkout-completed` event published
- ✅ Notification Service: Sends thank-you + feedback emails

---

## Common Issues & Solutions

### Issue 1: "Bill not found for bookingId: X"
**Error Message:** 
```
Bill not found for bookingId: 7. A bill is only generated when a booking is confirmed. 
Please confirm the booking first using POST /api/bookings/7/confirm
```

**Cause:** Booking not confirmed yet  
**Solution:** 
1. First, confirm the booking: `POST /api/bookings/7/confirm`
2. Wait a few seconds for the Kafka event to be processed
3. Then try getting the bill again: `GET /api/bills/booking/7`

**Why this happens:**
- When you create a booking, it starts with status `CREATED`
- Bills are only generated when booking status becomes `CONFIRMED`
- The `booking-confirmed` event triggers bill generation
- If you try to get a bill before confirming, it won't exist yet

### Issue 2: "Only CONFIRMED bookings can be checked in"
**Cause:** Trying to check-in a CREATED booking  
**Solution:** Confirm the booking first

### Issue 3: "Bill already exists"
**Cause:** Booking already confirmed  
**Solution:** This is normal - one booking = one bill

### Issue 4: "Bill is already paid"
**Cause:** Trying to mark an already-paid bill as paid  
**Solution:** Check bill status first

---

## Database States

### Booking States:
- `CREATED` → Initial state after creation
- `CONFIRMED` → After payment/confirmation (bill generated)
- `CHECKED_IN` → Guest has checked in
- `CHECKED_OUT` → Guest has checked out
- `CANCELLED` → Booking cancelled

### Bill States:
- `PENDING` → Bill generated, awaiting payment
- `PAID` → Payment received

---

## Production Flow (When Payment Service Exists)

In production, the flow would be:

1. User creates booking → `CREATED`
2. Payment Service processes payment
3. Payment Service calls `confirmBooking()` internally
4. Booking → `CONFIRMED`
5. Billing Service generates bill automatically
6. Admin marks bill as paid when payment is verified

For now, use the `/confirm` endpoint to simulate payment completion!

