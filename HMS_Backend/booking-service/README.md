# Booking Service

## What This Does

This service handles all the booking stuff - checking availability, creating bookings, cancellations, check-in, check-out. Basically everything related to reservations.

## Service Ownership

Just to be clear:
- **Hotel Service** owns: hotel data, room types, static information
- **Booking Service** owns: actual bookings, availability calculations, date overlaps

## How Booking Flow Works

1. **Search Hotels** - User searches via Hotel Service (public endpoint)
2. **View Hotel Details** - Get hotel info (public)
3. **Check Availability** - This service checks which rooms are available (public)
4. **Create Booking** - User creates booking (needs authentication)
5. **Background Processing** - Kafka events trigger notifications and billing

## API Endpoints

### Public Endpoints

#### Check Availability
```
GET /bookings/check-availability?hotelId=1&checkIn=2024-12-02&checkOut=2024-12-03
```
This calls Hotel Service to get rooms, then checks our database for overlapping bookings, and returns available rooms.

### Protected Endpoints

#### Create Booking
```
POST /bookings
```
Need to be logged in. Validates hotel exists, checks availability again (to prevent race conditions), creates booking with CONFIRMED status, and publishes Kafka event.

#### Get Booking by ID
```
GET /bookings/{bookingId}
```
- Users can only see their own bookings
- ADMIN/MANAGER can see any booking

#### Get My Bookings
```
GET /bookings/my-bookings
```
Returns all bookings for the logged-in user.

#### Get Bookings by Hotel
```
GET /bookings/hotel/{hotelId}
```
Staff only. They can only see bookings for their hotel.

#### Get All Bookings
```
GET /bookings
```
ADMIN only.

#### Cancel Booking
```
POST /bookings/{bookingId}/cancel
```
- GUEST can cancel their own bookings
- ADMIN can cancel any booking
- Can't cancel if already checked in or checked out

#### Check-In Guest
```
POST /bookings/{bookingId}/check-in
```
Staff only. Changes status to CHECKED_IN. Can only check in on the exact check-in date (not before, not after). Publishes Kafka events.

#### Check-Out Guest
```
POST /bookings/{bookingId}/check-out
```
Staff only. Changes status to CHECKED_OUT.

#### Get Today's Check-Ins
```
GET /bookings/hotel/{hotelId}/today-checkins
```
Returns all check-ins scheduled for today.

#### Get Today's Check-Outs
```
GET /bookings/hotel/{hotelId}/today-checkouts
```

## Business Rules

### Availability Calculation
1. Gets all active rooms from Hotel Service
2. Filters rooms with status = AVAILABLE
3. Excludes rooms with overlapping bookings (that aren't cancelled)
4. Returns available rooms

### Booking Creation
1. Validates hotel exists and is ACTIVE
2. Validates room belongs to hotel and is available
3. Double-checks availability (prevents race conditions)
4. Calculates total amount (price × nights)
5. Creates booking with status CONFIRMED
6. Publishes `booking-created` Kafka event

### Check-In Rules
- Can only check in on the exact check-in date
- Booking must be CONFIRMED status
- Staff can only check in guests at their hotel

### Walk-In Bookings
- Can only be created for today's date (not future, not past)
- Created by receptionist/staff
- Check-out must be after check-in date

## Authorization

- **GUEST** - Can create bookings, view own bookings, cancel own bookings
- **MANAGER/RECEPTIONIST** - Can view hotel bookings, check-in/out guests, see today's operations
- **ADMIN** - Full access to everything

Staff can only access bookings for their assigned hotel (from JWT token).

## Kafka Events

### booking-created
Published when a new booking is created. Contains booking details.

### booking-checked-in
Published when guest checks in.

### booking-completed
Published after check-in.

## Database Setup

```sql
CREATE DATABASE hms_booking_db;
```

## Configuration

```properties
spring.application.name=booking-service
server.port=9003

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/hms_booking_db
spring.datasource.username=root
spring.datasource.password=Ish983556

# Eureka
eureka.client.service-url.defaultZone=http://localhost:8761/eureka

# Kafka
spring.kafka.bootstrap-servers=localhost:9092
```

## Integration Points

- **Hotel Service** - Gets hotel and room details via Feign
- **Kafka** - Publishes events for notifications and billing
- **Billing Service** - Listens to booking events

## Error Handling

Common errors:
- `400` - Bad Request (validation errors)
- `401` - Unauthorized (missing/invalid token)
- `403` - Forbidden (insufficient permissions)
- `404` - Not Found (booking/hotel not found)
- `500` - Internal Server Error

## Testing

1. Check availability: `GET /bookings/check-availability?hotelId=1&checkIn=2024-12-02&checkOut=2024-12-03`
2. Create booking: `POST /bookings` (need to be logged in)
3. View booking: `GET /bookings/{bookingId}`
4. Check-in: `POST /bookings/{bookingId}/check-in` (staff only)

## Important Notes

- Walk-in bookings can only be created for today
- Check-in can only happen on the exact check-in date
- Bookings are created with CONFIRMED status automatically
- Kafka events are published for notifications and billing

