# Hotel Service

## Overview

This service manages hotels, rooms, and room availability. It's basically the core service that handles all hotel-related data. It also helps create staff users by talking to Auth Service.

## What It Does

1. **Hotel Management** - Create, update, search hotels
2. **Room Management** - Create, update, delete rooms, manage room status
3. **Availability** - Block/unblock rooms, search availability
4. **Staff Creation** - Creates staff users via Auth Service
5. **Public Search** - Guests can search hotels without logging in

## Service Ownership

Just to clarify:
- **Hotel Service** owns: hotel data, hotel status (ACTIVE/INACTIVE), room types and capacity
- **Booking Service** owns: actual bookings, availability calculations, date overlaps

## Authorization

- **ADMIN** - Can access all hotels
- **MANAGER** - Can only access their assigned hotel (from JWT token)
- **RECEPTIONIST** - Can only access their assigned hotel

## API Endpoints

### Public Endpoints (No Auth Required)

#### Search Hotels
```
GET /hotels/search?city=MUMBAI
GET /hotels/search?category=LUXURY
```
You can search by city or category.

#### Get Hotel Details
```
GET /hotels/{hotelId}
```
Get full details of a specific hotel.

#### Search Room Availability
```
GET /hotels/availability/search?hotelId=1&checkIn=2026-01-15&checkOut=2026-01-17
```
Check which rooms are available for given dates.

### Protected Endpoints

#### Create Hotel (Admin Only)
```
POST /hotels
```
Need admin token. Send hotel details in JSON body.

#### Update Hotel
```
PUT /hotels/{hotelId}
```
- Admin can update any hotel
- Manager can only update their own hotel

#### Get My Hotel (For Staff)
```
GET /hotels/my-hotel
```
Returns the hotel that the staff member is assigned to.

#### Get All Hotels (Admin Only)
```
GET /hotels
```

### Room Management

#### Create Room
```
POST /hotels/rooms
```
- Admin can create rooms for any hotel
- Manager can only create rooms for their hotel

#### Get Rooms by Hotel
```
GET /hotels/rooms/hotel/{hotelId}
```

#### Get Room by ID
```
GET /hotels/rooms/{roomId}
```

#### Update Room
```
PUT /hotels/rooms/{roomId}
```
- Admin can update any room
- Manager can only update rooms in their hotel

#### Delete Room (Soft Delete)
```
DELETE /hotels/rooms/{roomId}
```
Sets `isActive = false` instead of actually deleting.

#### Update Room Status
```
PATCH /hotels/rooms/{roomId}/status?status=MAINTENANCE
```
Status can be: AVAILABLE, OCCUPIED, MAINTENANCE, OUT_OF_SERVICE

#### Update Room Active Status
```
PATCH /hotels/rooms/{roomId}/active?active=false
```
Activate or deactivate a room.

### Availability Management

#### Block Room
```
POST /hotels/availability/block
```
Block a room for maintenance or other reasons.

#### Unblock Room
```
POST /hotels/availability/unblock
```

### Staff Creation

#### Create Staff User
```
POST /hotels/{hotelId}/staff
```
Admin only. Creates a staff user (MANAGER or RECEPTIONIST) bound to the hotel.

## Database Setup

```sql
CREATE DATABASE hms_hotel_db;
```

## Configuration

```properties
spring.application.name=hotel-service
server.port=9002

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/hms_hotel_db
spring.datasource.username=root
spring.datasource.password=Ish983556

# Eureka
eureka.client.service-url.defaultZone=http://localhost:8761/eureka
```

## Key Features

1. **Context-Aware Authorization** - Staff can only see their hotel
2. **Soft Delete** - Rooms are marked inactive, not actually deleted
3. **Availability Management** - Can block rooms for maintenance
4. **Public Search** - Guests can search without login
5. **Staff Creation** - Creates staff via Hotel Service → Auth Service

## Integration Points

- **Auth Service** - Creates staff users via Feign
- **Booking Service** - Booking service fetches hotel/room details
- **Notification Service** - Gets hotel details for emails
- **Reports Service** - Reads hotel data for dashboards

## Authorization Rules

### Hotels
- Create: ADMIN only
- Update: ADMIN (any), MANAGER (own hotel only)
- View: Public (search), ADMIN (all), Staff (own hotel)

### Rooms
- Create: ADMIN (any), MANAGER (own hotel)
- Update: ADMIN (any), MANAGER (own hotel)
- Delete: ADMIN (any), MANAGER (own hotel)
- Change Status: ADMIN, MANAGER, RECEPTIONIST (own hotel)

## Testing

1. Login as ADMIN
2. Create hotel: `POST /hotels`
3. Create staff: `POST /hotels/{hotelId}/staff`
4. Login as MANAGER
5. Create room: `POST /hotels/rooms`
6. Update room: `PUT /hotels/rooms/{roomId}`

## Common Errors

- `IllegalStateException` - Authorization failures, hotel not found
- `IllegalArgumentException` - Invalid parameters
- Missing required fields will also throw errors

