# Billing Service Troubleshooting Guide

## Problem: "Bill not found for bookingId: X"

### Root Causes

1. **Booking not confirmed** (most common)
   - Booking status is `CREATED` instead of `CONFIRMED`
   - Bills are only generated when booking is `CONFIRMED`

2. **Kafka event not processed**
   - `booking-confirmed` event was published but not received by Billing Service
   - Kafka consumer might be down or not connected

3. **Event processing error**
   - Event was received but bill generation failed silently
   - Check billing service logs for errors

## Solutions

### Solution 1: Check Booking Status

First, verify the booking exists and its status:

```
GET /api/bookings/8
```

**Expected Response:**
```json
{
  "data": {
    "id": 8,
    "status": "CONFIRMED",  // ← Must be CONFIRMED
    ...
  }
}
```

**If status is `CREATED`:**
- Confirm the booking: `POST /api/bookings/8/confirm`
- Wait 2-3 seconds for Kafka event processing
- Try getting bill again: `GET /api/bills/booking/8`

### Solution 2: Manually Generate Bill (Recovery)

If booking is `CONFIRMED` but bill doesn't exist, manually generate it:

```
POST /api/bills/generate/8
Headers:
  Authorization: Bearer <admin-token>
```

This endpoint:
- Checks if booking exists and is `CONFIRMED`
- Generates the bill if it doesn't exist
- Returns the bill details

**Use this when:**
- Kafka event was missed
- Event processing failed
- Testing/recovery scenarios

### Solution 3: Check Kafka Logs

Check billing service logs for:

1. **Event received:**
   ```
   Received booking-confirmed event: {...}
   ```

2. **Bill generated:**
   ```
   Bill generated for bookingId: 8, billId: 1
   ```

3. **Errors:**
   ```
   Error processing booking-confirmed event: ...
   Error generating bill for bookingId: 8
   ```

### Solution 4: Verify Kafka Connection

Ensure:
- Kafka is running: `localhost:9092`
- Billing service is connected to Kafka
- Consumer group `billing-group` is active
- Topic `booking-confirmed` exists

## Complete Testing Flow

### Step 1: Create Booking
```
POST /api/bookings
→ Returns bookingId (e.g., 8)
```

### Step 2: Verify Booking Status
```
GET /api/bookings/8
→ Check status field
```

### Step 3: Confirm Booking (if status is CREATED)
```
POST /api/bookings/8/confirm
→ Changes status to CONFIRMED
→ Publishes booking-confirmed event
```

### Step 4: Wait for Event Processing
Wait 2-3 seconds for Kafka to process the event.

### Step 5: Get Bill
```
GET /api/bills/booking/8
→ Should return bill details
```

### Step 6: If Bill Still Not Found - Manual Generation
```
POST /api/bills/generate/8
→ Manually generates bill for confirmed booking
```

## Error Messages Explained

### Error 1: "Bill not found for bookingId: 8"
**Meaning:** Bill doesn't exist in database

**Possible causes:**
- Booking not confirmed
- Kafka event not processed
- Event processing failed

**Action:**
1. Check booking status
2. If confirmed, manually generate bill
3. Check Kafka logs

### Error 2: "Booking status is: CREATED"
**Meaning:** Booking exists but not confirmed

**Action:**
```
POST /api/bookings/8/confirm
```

### Error 3: "Booking is CONFIRMED but bill was not generated"
**Meaning:** Kafka event issue

**Action:**
```
POST /api/bills/generate/8
```

## Quick Diagnostic Checklist

- [ ] Booking exists? → `GET /api/bookings/8`
- [ ] Booking is CONFIRMED? → Check `status` field
- [ ] Kafka running? → Check `localhost:9092`
- [ ] Billing service logs show event received?
- [ ] Try manual bill generation → `POST /api/bills/generate/8`

## New Endpoints Added

### Manual Bill Generation
```
POST /api/bills/generate/{bookingId}
Authorization: Bearer <admin-token>
```

**What it does:**
- Fetches booking details from Booking Service
- Verifies booking is `CONFIRMED`
- Generates bill if it doesn't exist
- Returns bill details

**Use cases:**
- Recovery from missed Kafka events
- Testing scenarios
- Manual bill generation for confirmed bookings

## Improved Error Messages

The error message now includes:
- Booking status check
- Helpful instructions
- Next steps to resolve

Example:
```
Bill not found for bookingId: 8. 
Booking status is: CREATED. 
A bill is only generated when a booking is CONFIRMED. 
Please confirm the booking first using POST /api/bookings/8/confirm
```

