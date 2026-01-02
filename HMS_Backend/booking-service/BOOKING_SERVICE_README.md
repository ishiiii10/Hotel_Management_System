# Booking Service Implementation

## Overview
The Booking Service handles all booking-related operations including availability checks, booking creation, cancellation, check-in, and check-out.

## Architecture

### Service Ownership
- **Hotel Service** owns: hotel existence, hotel status (ACTIVE/INACTIVE), room types/capacity (static data)
- **Booking Service** owns: bookings, availability calculations, date overlaps, room inventory usage

### Flow

#### 1. Search → Book Flow

**Step 1: Search Hotels** (Hotel Service - Public)
```
GET /hotels/search?city=Mumbai
```

**Step 2: View Hotel Details** (Hotel Service - Public)
```
GET /hotels/{hotelId}
```

**Step 3: Check Availability** (Booking Service - Public)
```
GET /bookings/check-availability?hotelId=1&checkIn=2024-12-02&checkOut=2024-12-03
```
- Calls Hotel Service via Feign to get all rooms
- Queries own DB for overlapping bookings
- Filters available rooms (excludes booked and unavailable rooms)

**Step 4: Create Booking** (Booking Service - Protected)
```
POST /bookings
```
- Validates hotel exists and is active
- Double-checks availability (prevents race conditions)
- Creates booking with status CONFIRMED
- Publishes `booking-created` Kafka event

**Step 5: Background Processing** (Kafka)
- Notification Service: Sends email/SMS
- Payment Service: Processes payment

## API Endpoints

### Public Endpoints

#### 1. Check Availability
```
GET /bookings/check-availability?hotelId={hotelId}&checkIn={date}&checkOut={date}
```
**Response:**
```json
{
  "success": true,
  "message": "Availability check completed successfully",
  "data": {
    "hotelId": 1,
    "totalRooms": 10,
    "availableRooms": 5,
    "availableRoomsList": [
      {
        "roomId": 1,
        "roomNumber": "101",
        "roomType": "DELUXE",
        "pricePerNight": 5000.00,
        "maxOccupancy": 2,
        "amenities": "WiFi, TV, AC",
        "description": "Spacious room"
      }
    ]
  }
}
```

### Protected Endpoints

#### 2. Create Booking
```
POST /bookings
Authorization: Bearer <token>
```
**Request Body:**
```json
{
  "hotelId": 1,
  "roomId": 102,
  "checkInDate": "2024-12-02",
  "checkOutDate": "2024-12-03",
  "numberOfGuests": 2,
  "rooms": 1,
  "specialRequests": "Late check-in requested"
}
```
**Response:**
```json
{
  "success": true,
  "message": "Booking created successfully",
  "data": {
    "id": 123,
    "userId": 5,
    "hotelId": 1,
    "roomId": 102,
    "status": "CONFIRMED",
    "totalAmount": 5000.00,
    ...
  }
}
```

#### 3. Get Booking by ID
```
GET /bookings/{bookingId}
Authorization: Bearer <token>
```
- Users can only view their own bookings
- ADMIN/MANAGER can view any booking

#### 4. Get My Bookings
```
GET /bookings/my-bookings
Authorization: Bearer <token>
```
Returns all bookings for the authenticated user.

#### 5. Get Bookings by Hotel
```
GET /bookings/hotel/{hotelId}
Authorization: Bearer <token>
```
- Staff only (MANAGER, RECEPTIONIST, ADMIN)
- Staff can only view bookings for their assigned hotel

#### 6. Get All Bookings
```
GET /bookings
Authorization: Bearer <token>
```
- ADMIN only

#### 7. Cancel Booking
```
POST /bookings/{bookingId}/cancel
Authorization: Bearer <token>
```
**Request Body:**
```json
{
  "reason": "Change of plans"
}
```
- GUEST can cancel their own bookings
- ADMIN can cancel any booking
- Cannot cancel CHECKED_IN or CHECKED_OUT bookings

#### 8. Check-In Guest
```
POST /bookings/{bookingId}/check-in
Authorization: Bearer <token>
```
**Request Body (optional):**
```json
{
  "notes": "Guest arrived early"
}
```
- Staff only (MANAGER, RECEPTIONIST, ADMIN)
- Changes status to CHECKED_IN
- Publishes `booking-checked-in` and `booking-completed` events

#### 9. Check-Out Guest
```
POST /bookings/{bookingId}/check-out
Authorization: Bearer <token>
```
**Request Body (optional):**
```json
{
  "notes": "Guest requested late checkout",
  "rating": 5,
  "feedback": "Excellent service",
  "lateCheckOut": true
}
```
- Staff only (MANAGER, RECEPTIONIST, ADMIN)
- Changes status to CHECKED_OUT

#### 10. Get Today's Check-Ins
```
GET /bookings/hotel/{hotelId}/today-checkins
Authorization: Bearer <token>
```
- Staff only
- Returns all check-ins scheduled for today

#### 11. Get Today's Check-Outs
```
GET /bookings/hotel/{hotelId}/today-checkouts
Authorization: Bearer <token>
```
- Staff only
- Returns all check-outs scheduled for today

## Business Rules

### Availability Calculation
1. Gets all active rooms from Hotel Service
2. Filters rooms with status = AVAILABLE
3. Excludes rooms with overlapping bookings (non-CANCELLED)
4. Returns available rooms with details

### Booking Creation
1. Validates hotel exists and is ACTIVE
2. Validates room belongs to hotel and is available
3. Double-checks availability (prevents race conditions)
4. Calculates total amount (pricePerNight × nights)
5. Creates booking with status CONFIRMED
6. Publishes Kafka event

### Authorization
- **GUEST**: Can create bookings, view own bookings, cancel own bookings
- **MANAGER/RECEPTIONIST**: Can view hotel bookings, check-in/out guests, view today's operations
- **ADMIN**: Full access to all operations

### Context-Aware Authorization
- Staff can only access bookings for their assigned hotel
- Hotel ID is validated from JWT token (X-Hotel-Id header)

## Kafka Events

### booking-created
Published when a new booking is created.
```json
{
  "bookingId": 123,
  "userId": 5,
  "hotelId": 1,
  "roomId": 102,
  "checkInDate": "2024-12-02",
  "checkOutDate": "2024-12-03",
  "amount": 5000.0
}
```

### booking-checked-in
Published when a guest checks in.
```json
{
  "bookingId": 123,
  "userId": 5,
  "hotelId": 1
}
```

### booking-completed
Published when a booking is completed (after check-in).
```json
{
  "bookingId": 123,
  "userId": 5,
  "hotelId": 1
}
```

## Database Schema

### Booking Entity
- `id`: Primary key
- `user_id`: Foreign key to users
- `hotel_id`: Foreign key to hotels
- `room_id`: Foreign key to rooms
- `check_in_date`: Check-in date
- `check_out_date`: Check-out date
- `total_amount`: Calculated total
- `status`: PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED
- `guest_name`, `guest_email`, `guest_phone`: Guest information
- `number_of_guests`: Number of guests
- `number_of_nights`: Calculated automatically
- `special_requests`: Optional special requests
- `cancellation_reason`: Reason for cancellation
- `cancelled_at`, `checked_in_at`, `checked_out_at`: Timestamps
- `created_at`, `updated_at`: Audit timestamps

## Integration Points

### Hotel Service (Feign Client)
- `GET /hotels/{hotelId}` - Get hotel details
- `GET /hotels/rooms/hotel/{hotelId}` - Get all rooms for hotel
- `GET /hotels/rooms/{roomId}` - Get room details

### Kafka Topics
- `booking-created` - New booking created
- `booking-checked-in` - Guest checked in
- `booking-completed` - Booking completed

## Error Handling

All errors follow standard format:
```json
{
  "success": false,
  "message": "Error description",
  "errors": ["Detailed error messages"]
}
```

### Common Errors
- `400`: Bad Request (validation errors)
- `401`: Unauthorized (missing/invalid token)
- `403`: Forbidden (insufficient permissions)
- `404`: Not Found (booking/hotel not found)
- `500`: Internal Server Error

