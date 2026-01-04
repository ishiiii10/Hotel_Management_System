# Kafka Event Troubleshooting Guide

## Problem: Events Published But Not Received

### Symptoms
- ✅ Booking service logs: "Published BookingConfirmedEvent for bookingId: 9"
- ❌ Billing service logs: **NO** "Received booking-confirmed event" message
- ❌ Bill not generated

### Root Causes

1. **Billing Service Not Running or Not Connected to Kafka**
   - Service might be down
   - Kafka connection failed
   - Consumer group not active

2. **Kafka Not Running**
   - Kafka server not started
   - Wrong bootstrap server address

3. **Consumer Group Offset Issue**
   - Consumer already processed events and moved offset forward
   - New events published after consumer started

4. **Topic/Partition Mismatch**
   - Topic doesn't exist
   - Wrong topic name

## Immediate Solution: Manual Bill Generation

Since the Kafka event wasn't received, use the manual bill generation endpoint:

```bash
POST /api/bills/generate/9
Headers:
  Authorization: Bearer <admin-token>
```

This will:
- Fetch booking details from Booking Service
- Verify booking is CONFIRMED
- Generate the bill directly
- Return bill details

## Diagnostic Steps

### Step 1: Check Kafka is Running
```bash
# Check if Kafka is running
ps aux | grep kafka

# Or check Kafka port
netstat -an | grep 9092
```

### Step 2: Check Billing Service Logs

Look for:
- ✅ "Kafka listener triggered for booking-confirmed topic"
- ✅ "Received booking-confirmed event"
- ✅ "Bill successfully generated"

If you see **NONE** of these, the listener isn't receiving events.

### Step 3: Verify Consumer Group

```bash
# List consumer groups
kafka-consumer-groups --bootstrap-server localhost:9092 --list

# Check billing-group status
kafka-consumer-groups --bootstrap-server localhost:9092 \
  --group billing-group --describe
```

### Step 4: Check Topic Exists

```bash
# List topics
kafka-topics --bootstrap-server localhost:9092 --list

# Should see: booking-confirmed
```

### Step 5: Test Kafka Connection

Check billing service logs for:
- Kafka connection errors
- Consumer initialization messages
- Any Kafka-related exceptions

## Common Fixes

### Fix 1: Restart Billing Service

The service might need a restart to:
- Reconnect to Kafka
- Reset consumer group
- Apply configuration changes

```bash
# Stop billing service
# Start billing service
# Check logs for "Kafka listener" initialization
```

### Fix 2: Reset Consumer Group Offset

If events were published before consumer started:

```bash
# Reset offset to earliest
kafka-consumer-groups --bootstrap-server localhost:9092 \
  --group billing-group \
  --topic booking-confirmed \
  --reset-offsets \
  --to-earliest \
  --execute
```

### Fix 3: Verify Configuration

Check `application.properties`:
```properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=billing-group
spring.kafka.consumer.auto-offset-reset=earliest
```

### Fix 4: Check Service Registration

Ensure billing service is registered with Eureka:
- Check Eureka dashboard: http://localhost:8761
- Look for `BILLING-SERVICE` in registered services

## Enhanced Logging

The billing service now logs:
- When listener is triggered
- Event payload received
- Bill generation steps
- Success/failure messages

Look for these log patterns:
```
INFO  ... Kafka listener triggered for booking-confirmed topic
INFO  ... Received booking-confirmed event: {...}
INFO  ... generateBill called for bookingId: 9
INFO  ... ✅ Bill successfully generated! bookingId: 9, billId: 1
```

## Quick Recovery

**For bookingId 9 (or any confirmed booking):**

```bash
# Option 1: Manual bill generation (immediate)
POST /api/bills/generate/9

# Option 2: Check if booking is confirmed first
GET /api/bookings/9
# If status is CONFIRMED, use Option 1
```

## Prevention

1. **Always restart services after code changes**
2. **Monitor Kafka consumer lag**
3. **Use manual generation as fallback**
4. **Check logs regularly for Kafka errors**

## Testing Kafka Connection

To verify Kafka is working:

1. **Check booking service publishes:**
   ```
   Published BookingConfirmedEvent for bookingId: 9
   ```

2. **Check billing service receives:**
   ```
   Kafka listener triggered for booking-confirmed topic
   Received booking-confirmed event: {...}
   ```

3. **If step 2 is missing:**
   - Kafka connection issue
   - Consumer not subscribed
   - Use manual generation endpoint

