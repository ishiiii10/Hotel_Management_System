# Hotel Management System API Documentation

## Base URL
- **API Gateway**: `http://localhost:9090`
- **Auth Service**: `http://localhost:9001` (direct access)
- **Hotel Service**: `http://localhost:9002` (direct access)

## System Invariants

The system enforces the following invariants:

### 1. Availability is Date-Based, Not Stored
- **Enforcement**: Availability is calculated dynamically based on date ranges
- **Implementation**: `RoomAvailability` table stores per-date status, but availability is computed by querying all dates in a range
- **Query**: `findAvailableRoomIdsStrict()` checks ALL dates in the range to ensure room is available for the entire period

### 2. Room Status Affects Availability
- **Enforcement**: Only rooms with `status = AVAILABLE` and `isActive = true` are considered
- **Implementation**: The availability query filters out rooms with status:
  - `INACTIVE`
  - `MAINTENANCE`
  - `OUT_OF_SERVICE`
  - `OCCUPIED` (for current bookings)
- **Query Filter**: `r.status = 'AVAILABLE' AND r.isActive = true`

### 3. Bookings Affect Availability Only When Overlapping
- **Enforcement**: Rooms with `RESERVED` status in `RoomAvailability` are excluded from availability
- **Implementation**: The query excludes rooms where any date in the range has `status = 'RESERVED'`
- **Future Integration**: Booking service will set `RESERVED` status for overlapping dates

### 4. Blocked Rooms Reduce Availability Immediately
- **Enforcement**: When a room is blocked, `RoomAvailability` status is set to `BLOCKED` immediately
- **Implementation**: `blockRoom()` endpoint updates availability status to `BLOCKED` for the specified date range
- **Query Filter**: Rooms with `BLOCKED` status in any date of the range are excluded

### 5. Search Results Must Reflect Availability for Given Dates
- **Enforcement**: Hotel search uses `searchAvailability()` to get accurate room counts
- **Implementation**: 
  - When `checkIn` and `checkOut` are provided, availability is calculated for that date range
  - Hotels with 0 available rooms are filtered out from search results
- **Query**: `searchHotels()` calls `availabilityService.searchAvailability()` with date parameters

### 6. Staff Can Only Affect Their Own Hotel
- **Enforcement**: All hotel/room operations check `X-Hotel-Id` header against the resource's hotelId
- **Implementation**: 
  - `HotelController.updateHotel()`: MANAGER can only update their assigned hotel
  - `RoomController`: All operations verify staff's hotelId matches room's hotelId
  - `RoomAvailabilityController`: MANAGER can only block/unblock rooms in their hotel
- **Authorization**: Context-aware checks in all controllers

## Authentication

### Getting a Token
1. **Login** (POST `/auth/login`)
   ```json
   {
     "email": "admin@hotelbooking.com",
     "password": "admin123"
   }
   ```
2. **Response** includes `token` - use this in `Authorization: Bearer <token>` header

### Default Admin Credentials
- **Email**: `admin@hotelbooking.com`
- **Password**: `admin123`
- **Username**: `admin`

## Postman Collection Usage

### Setup
1. Import `Hotel_Management_System_Postman_Collection.json` into Postman
2. Set collection variables:
   - `base_url`: `http://localhost:9090`
   - `auth_token`: (will be set after login)
   - `admin_token`: (set after admin login)
   - `manager_token`: (set after manager login)
   - `receptionist_token`: (set after receptionist login)

### Workflow

#### 1. Admin Setup
1. **Login as Admin** → Copy token to `admin_token` variable
2. **Create Hotel** → Note the `hotelId` from response
3. **Create Staff** → Use the `hotelId` from step 2
   - Creates MANAGER or RECEPTIONIST
   - Returns `activationToken`
4. **Activate User** → Use the `activationToken` from step 3

#### 2. Staff Login
1. **Login as Manager/Receptionist** → Copy token to respective variable
2. Staff can now access their hotel's resources

#### 3. Hotel Operations
1. **Create Rooms** → Only for staff's assigned hotel
2. **Block/Unblock Rooms** → Only for staff's assigned hotel
3. **Update Room Status** → Only for staff's assigned hotel

## API Endpoints

### Auth Service

#### Public Endpoints
- `POST /auth/register/guest` - Guest registration
- `POST /auth/login` - User login
- `POST /auth/activate` - Activate staff account

#### Protected Endpoints (Require JWT)
- `GET /auth/me` - Get current user info
- `POST /auth/change-password` - Change password
- `GET /auth/admin/users` - List all users (ADMIN only)
- `PATCH /auth/admin/users/:userId/deactivate` - Deactivate user (ADMIN only)
- `PUT /auth/admin/staff/:userId/hotel-allotment` - Reassign staff (ADMIN only)

### Hotel Service

#### Public Endpoints
- `GET /hotels/search?city=...` - Search hotels by city
- `GET /hotels/search?category=...` - Search hotels by category
- `GET /hotels/:hotelId` - Get hotel details
- `GET /hotels/rooms/hotel/:hotelId` - Get rooms by hotel
- `GET /hotels/rooms/:roomId` - Get room details
- `GET /hotels/availability/search?hotelId=...&checkIn=...&checkOut=...` - Search availability

#### Protected Endpoints (Require JWT)

**Hotels**
- `POST /hotels` - Create hotel (ADMIN only)
- `PUT /hotels/:hotelId` - Update hotel (ADMIN/MANAGER)
- `GET /hotels` - Get all hotels (ADMIN only)
- `GET /hotels/my-hotel` - Get staff's assigned hotel
- `POST /hotels/:hotelId/staff` - Create staff for hotel (ADMIN only)

**Rooms**
- `POST /hotels/rooms` - Create room (ADMIN/MANAGER)
- `PUT /hotels/rooms/:roomId` - Update room (ADMIN/MANAGER)
- `PATCH /hotels/rooms/:roomId/status` - Update room status (ADMIN/MANAGER/RECEPTIONIST)
- `PATCH /hotels/rooms/:roomId/active` - Update room active status (ADMIN/MANAGER)
- `DELETE /hotels/rooms/:roomId` - Delete room (ADMIN/MANAGER)

**Availability**
- `POST /hotels/availability/block` - Block room (ADMIN/MANAGER)
- `POST /hotels/availability/unblock` - Unblock room (ADMIN/MANAGER)

## Request/Response Examples

### Create Hotel
**Request:**
```json
POST /hotels
Authorization: Bearer <admin_token>

{
  "name": "Grand Hotel",
  "category": "LUXURY",
  "description": "A luxurious 5-star hotel",
  "city": "MUMBAI",
  "address": "123 Main Street",
  "state": "MAHARASHTRA",
  "country": "India",
  "pincode": "400001",
  "contactNumber": "+91-22-12345678",
  "email": "info@grandhotel.com",
  "starRating": 5,
  "amenities": "WiFi, Pool, Gym, Spa",
  "status": "ACTIVE",
  "imageUrl": "https://example.com/hotel.jpg"
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

### Create Staff
**Request:**
```json
POST /hotels/1/staff
Authorization: Bearer <admin_token>

{
  "fullName": "Jane Manager",
  "username": "janemanager",
  "email": "jane.manager@hotel.com",
  "password": "manager123",
  "role": "MANAGER"
}
```

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

### Search Availability
**Request:**
```
GET /hotels/availability/search?hotelId=1&checkIn=2024-12-25&checkOut=2024-12-30
```

**Response:**
```json
{
  "success": true,
  "message": "Availability search completed successfully",
  "data": {
    "hotelId": 1,
    "availableRooms": 5,
    "availableRoomIds": [1, 2, 3, 4, 5]
  }
}
```

## Error Responses

All errors follow this format:
```json
{
  "success": false,
  "message": "Error description",
  "errors": ["Detailed error 1", "Detailed error 2"]
}
```

### Common Error Codes
- `400` - Bad Request (validation errors)
- `401` - Unauthorized (missing/invalid token)
- `403` - Forbidden (insufficient permissions)
- `404` - Not Found (resource doesn't exist)
- `500` - Internal Server Error

## Notes

1. **Date Format**: All dates use `YYYY-MM-DD` format
2. **Check-out Date**: The `checkOut` parameter is exclusive (guest doesn't stay on that date)
3. **Token Expiry**: JWT tokens expire after 60 minutes (configurable)
4. **Staff Activation**: Staff users must be activated before they can login
5. **Hotel Binding**: Staff users are permanently bound to a hotel at creation time

