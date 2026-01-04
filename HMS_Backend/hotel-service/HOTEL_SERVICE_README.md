# Hotel Service Implementation

## Overview
The Hotel Service manages hotels, rooms, room availability, and staff creation. It owns hotel and room data, and coordinates with Auth Service for staff user creation.

## Core Responsibilities

1. **Hotel Management**: Create, update, and search hotels
2. **Room Management**: Create, update, delete, and manage room status
3. **Availability Management**: Block/unblock rooms, search availability
4. **Staff Creation**: Create staff users via Auth Service (Feign)
5. **Public Search**: Hotel and room search for guests

## Architecture

### Service Ownership
- **Hotel Service** owns: hotel existence, hotel status (ACTIVE/INACTIVE), room types/capacity (static data)
- **Booking Service** owns: bookings, availability calculations, date overlaps, room inventory usage

### Context-Aware Authorization
- **ADMIN**: Can access all hotels
- **MANAGER**: Can only access their assigned hotel (from JWT `X-Hotel-Id`)
- **RECEPTIONIST**: Can only access their assigned hotel (from JWT `X-Hotel-Id`)

## API Endpoints

### Public Endpoints

#### 1. Search Hotels
```
GET /hotels/search?city=MUMBAI
GET /hotels/search?category=LUXURY
```
**Query Parameters:**
- `city`: City enum (MUMBAI, DELHI, BANGALORE, etc.)
- `category`: Hotel category (LUXURY, BUDGET, BUSINESS, etc.)

**Response:**
```json
{
  "success": true,
  "message": "Hotels search completed successfully",
  "data": [
    {
      "id": 1,
      "name": "Grand Plaza Hotel",
      "description": "Luxury hotel in Mumbai",
      "address": "123 Marine Drive",
      "city": "MUMBAI",
      "state": "MAHARASHTRA",
      "country": "India",
      "pincode": "400001",
      "contactNumber": "9876543210",
      "email": "contact@grandplaza.com",
      "starRating": 5,
      "amenities": "WiFi,Pool,Gym,Spa,Restaurant",
      "status": "ACTIVE",
      "totalRooms": 10,
      "availableRooms": 5
    }
  ]
}
```

#### 2. Get Hotel Details
```
GET /hotels/{hotelId}
```

**Response:**
```json
{
  "success": true,
  "message": "Hotel details retrieved successfully",
  "data": {
    "id": 1,
    "name": "Grand Plaza Hotel",
    "description": "Luxury hotel in Mumbai",
    "address": "123 Marine Drive",
    "city": "MUMBAI",
    "state": "MAHARASHTRA",
    "country": "India",
    "pincode": "400001",
    "contactNumber": "9876543210",
    "email": "contact@grandplaza.com",
    "starRating": 5,
    "amenities": "WiFi,Pool,Gym,Spa,Restaurant",
    "status": "ACTIVE",
    "totalRooms": 10,
    "availableRooms": 5,
    "createdAt": "2025-12-30T22:04:05",
    "updatedAt": "2025-12-30T22:10:25"
  }
}
```

#### 3. Search Room Availability
```
GET /hotels/availability/search?hotelId=1&checkIn=2026-01-15&checkOut=2026-01-17
```

**Response:**
```json
{
  "success": true,
  "message": "Availability search completed successfully",
  "data": {
    "hotelId": 1,
    "checkIn": "2026-01-15",
    "checkOut": "2026-01-17",
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

#### 4. Create Hotel (Admin Only)
```
POST /hotels
Authorization: Bearer <admin-token>
Content-Type: application/json
```
**Request Body:**
```json
{
  "name": "Grand Plaza Hotel",
  "category": "LUXURY",
  "description": "Luxury hotel in Mumbai",
  "city": "MUMBAI",
  "address": "123 Marine Drive",
  "state": "MAHARASHTRA",
  "country": "India",
  "pincode": "400001",
  "contactNumber": "9876543210",
  "email": "contact@grandplaza.com",
  "starRating": 5,
  "amenities": "WiFi,Pool,Gym,Spa,Restaurant"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Hotel created successfully",
  "data": {
    "id": 1
  }
}
```

#### 5. Update Hotel
```
PUT /hotels/{hotelId}
Authorization: Bearer <token>
Content-Type: application/json
```
- **ADMIN**: Can update any hotel
- **MANAGER**: Can only update their assigned hotel

#### 6. Get My Hotel (Staff)
```
GET /hotels/my-hotel
Authorization: Bearer <staff-token>
```
Returns hotel details for the staff member's assigned hotel.

#### 7. Get All Hotels (Admin Only)
```
GET /hotels
Authorization: Bearer <admin-token>
```

### Room Management Endpoints

#### 8. Create Room
```
POST /hotels/rooms
Authorization: Bearer <token>
Content-Type: application/json
```
**Request Body:**
```json
{
  "hotelId": 1,
  "roomNumber": "101",
  "roomType": "DELUXE",
  "pricePerNight": 5000.00,
  "maxOccupancy": 2,
  "amenities": "WiFi, TV, AC",
  "description": "Spacious room with sea view"
}
```

**Authorization:**
- ADMIN: Can create rooms for any hotel
- MANAGER: Can only create rooms for their assigned hotel

#### 9. Get Rooms by Hotel
```
GET /hotels/rooms/hotel/{hotelId}
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "hotelId": 1,
      "roomNumber": "101",
      "roomType": "DELUXE",
      "pricePerNight": 5000.00,
      "maxOccupancy": 2,
      "amenities": "WiFi, TV, AC",
      "description": "Spacious room",
      "status": "AVAILABLE",
      "isActive": true
    }
  ]
}
```

#### 10. Get Room by ID
```
GET /hotels/rooms/{roomId}
```

#### 11. Update Room
```
PUT /hotels/rooms/{roomId}
Authorization: Bearer <token>
Content-Type: application/json
```
- **ADMIN**: Can update any room
- **MANAGER**: Can only update rooms in their assigned hotel

#### 12. Delete Room (Soft Delete)
```
DELETE /hotels/rooms/{roomId}
Authorization: Bearer <token>
```
- Sets `isActive = false`
- **ADMIN**: Can delete any room
- **MANAGER**: Can only delete rooms in their assigned hotel

#### 13. Update Room Status
```
PATCH /hotels/rooms/{roomId}/status?status=MAINTENANCE
Authorization: Bearer <token>
```
**Status Values:** `AVAILABLE`, `OCCUPIED`, `MAINTENANCE`, `OUT_OF_SERVICE`

**Authorization:**
- ADMIN, MANAGER, RECEPTIONIST: Can change status
- Staff can only change status for rooms in their assigned hotel

#### 14. Update Room Active Status
```
PATCH /hotels/rooms/{roomId}/active?active=false
Authorization: Bearer <token>
```
- Activates/deactivates room
- **ADMIN**: Can update any room
- **MANAGER**: Can only update rooms in their assigned hotel

### Availability Management Endpoints

#### 15. Block Room
```
POST /hotels/availability/block
Authorization: Bearer <token>
Content-Type: application/json
```
**Request Body:**
```json
{
  "hotelId": 1,
  "roomId": 1,
  "startDate": "2026-01-20",
  "endDate": "2026-01-25",
  "reason": "Maintenance"
}
```

**Authorization:**
- ADMIN: Can block any room
- MANAGER: Can only block rooms in their assigned hotel

#### 16. Unblock Room
```
POST /hotels/availability/unblock
Authorization: Bearer <token>
Content-Type: application/json
```
**Request Body:**
```json
{
  "hotelId": 1,
  "roomId": 1,
  "startDate": "2026-01-20",
  "endDate": "2026-01-25"
}
```

### Staff Creation Endpoint

#### 17. Create Staff User
```
POST /hotels/{hotelId}/staff
Authorization: Bearer <admin-token>
Content-Type: application/json
```
**Request Body:**
```json
{
  "fullName": "Jane Manager",
  "username": "jane_manager",
  "email": "jane@hotel.com",
  "password": "SecurePass123!",
  "role": "MANAGER"
}
```

**Flow:**
1. Hotel Service validates hotel exists
2. Hotel Service validates user is ADMIN
3. Hotel Service calls Auth Service via Feign
4. Auth Service creates user with hotel binding
5. Returns activation token

**Response:**
```json
{
  "success": true,
  "message": "Staff user created successfully",
  "data": {
    "activationToken": "abc123def456..."
  }
}
```

## Internal Endpoints (Feign)

#### 18. Get Hotel by ID (Internal)
```
GET /internal/hotels/{hotelId}
```
**Used by:** Booking Service, Notification Service, Reports Service
**Returns:** Raw `HotelDetailResponse` without wrapper

#### 19. Get Rooms by Hotel (Internal)
```
GET /internal/hotels/{hotelId}/rooms
```
**Used by:** Booking Service
**Returns:** Raw `List<RoomResponse>` without wrapper

#### 20. Get Room by ID (Internal)
```
GET /internal/hotels/rooms/{roomId}
```
**Used by:** Booking Service
**Returns:** Raw `RoomResponse` without wrapper

## Domain Models

### Hotel Entity
- `id`: Primary key
- `name`: Hotel name
- `category`: LUXURY, BUDGET, BUSINESS, etc.
- `description`: Hotel description
- `city`: City enum
- `address`: Street address
- `state`: State enum
- `country`: Country name
- `pincode`: Postal code
- `email`: Contact email (unique)
- `contactNumber`: Phone number (unique)
- `starRating`: Star rating (1-5)
- `amenities`: Comma-separated amenities
- `imageUrl`: Hotel image URL
- `status`: ACTIVE or INACTIVE
- `createdAt`, `updatedAt`: Timestamps

### Room Entity
- `id`: Primary key
- `hotelId`: Foreign key to Hotel
- `roomNumber`: Room number (e.g., "101")
- `roomType`: DELUXE, STANDARD, SUITE, etc.
- `pricePerNight`: Room price
- `maxOccupancy`: Maximum guests
- `amenities`: Room amenities
- `description`: Room description
- `status`: AVAILABLE, OCCUPIED, MAINTENANCE, OUT_OF_SERVICE
- `isActive`: Soft delete flag
- `createdAt`, `updatedAt`: Timestamps

### RoomAvailability Entity
- `id`: Primary key
- `hotelId`: Foreign key to Hotel
- `roomId`: Foreign key to Room
- `date`: Availability date
- `status`: AVAILABLE, BLOCKED, RESERVED
- `reason`: Blocking reason (if blocked)

## Enums

### City
MUMBAI, DELHI, BANGALORE, KOLKATA, CHENNAI, HYDERABAD, PUNE, JAIPUR

### State
MAHARASHTRA, DELHI, KARNATAKA, WEST_BENGAL, TAMIL_NADU, TELANGANA, RAJASTHAN

### Hotel_Category
LUXURY, BUDGET, BUSINESS, BOUTIQUE, RESORT

### HotelStatus
ACTIVE, INACTIVE

### RoomCategory
DELUXE, STANDARD, SUITE, EXECUTIVE, FAMILY

### RoomStatus
AVAILABLE, OCCUPIED, MAINTENANCE, OUT_OF_SERVICE

### AvailabilityStatus
AVAILABLE, BLOCKED, RESERVED

## Database Setup

Create the database:
```sql
CREATE DATABASE hms_hotel_db;
```

## Configuration

### application.properties
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

1. **Context-Aware Authorization**: Staff can only access their assigned hotel
2. **Soft Delete**: Rooms are soft-deleted (isActive = false) instead of hard delete
3. **Availability Management**: Block/unblock rooms for maintenance or other reasons
4. **Public Search**: Guests can search hotels without authentication
5. **Staff Creation**: Centralized staff creation via Hotel Service → Auth Service

## Integration Points

### With Auth Service
- Creates staff users via Feign client
- Staff users are bound to hotels at creation time

### With Booking Service
- Booking Service fetches hotel and room details via Feign
- Used for availability calculations and booking creation

### With Notification Service
- Notification Service fetches hotel details for email notifications

### With Reports Service
- Reports Service reads hotel and room data for dashboard metrics

## Authorization Rules

### Hotel Operations
- **Create Hotel**: ADMIN only
- **Update Hotel**: ADMIN (any hotel), MANAGER (own hotel only)
- **View Hotels**: Public (search), ADMIN (all hotels), Staff (own hotel)

### Room Operations
- **Create Room**: ADMIN (any hotel), MANAGER (own hotel only)
- **Update Room**: ADMIN (any room), MANAGER (own hotel rooms only)
- **Delete Room**: ADMIN (any room), MANAGER (own hotel rooms only)
- **Change Status**: ADMIN, MANAGER, RECEPTIONIST (own hotel rooms only)

### Availability Operations
- **Block/Unblock**: ADMIN (any room), MANAGER (own hotel rooms only)
- **Search Availability**: Public (no authentication required)

## Error Handling

Common errors:
- `IllegalStateException`: Authorization failures, hotel not found
- `IllegalArgumentException`: Invalid parameters, missing required fields
- `IllegalStateException`: Context-aware authorization violations

## Testing

### Test Hotel Creation
1. Login as ADMIN
2. Create hotel: `POST /hotels`
3. Verify hotel: `GET /hotels/{hotelId}`

### Test Staff Creation
1. Login as ADMIN
2. Create hotel: `POST /hotels`
3. Create staff: `POST /hotels/{hotelId}/staff`
4. Staff user is created with hotel binding

### Test Room Management
1. Login as MANAGER
2. Create room: `POST /hotels/rooms` (for manager's hotel)
3. Update room: `PUT /hotels/rooms/{roomId}`
4. Block room: `POST /hotels/availability/block`

