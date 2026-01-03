# Reports & Dashboards Service

## Overview
The Reports Service provides operational dashboards for ADMIN and MANAGER roles. It is a read-only service that aggregates data from booking, billing, and hotel services.

## Architecture

### Multiple Data Sources
The service connects to three databases:
- **Booking Database**: For booking data
- **Billing Database**: For revenue and payment data
- **Hotel Database**: For room availability data

### Read-Only Design
- No data modifications
- No synchronous service calls
- Simple JPQL queries
- Direct database access

## Dashboard Metrics

### Numbers (Metrics)
1. **Total Revenue**: Sum of all PAID bills (all-time)
2. **Today Revenue**: Sum of PAID bills created today
3. **Total Bookings**: Total confirmed bookings (all-time)
4. **Today Bookings**: Bookings created today
5. **Check-ins Today**: Bookings with checkInDate = today
6. **Check-outs Today**: Bookings with checkOutDate = today
7. **Available Rooms Today**: Count of available rooms for today
8. **Average Rating**: Average hotel rating (currently hardcoded to 4.4)

### Graphs
1. **Today Revenue by Hour**: Revenue amount for each hour (0-23)
2. **Today Bookings by Source**: Bookings grouped by hour and source (PUBLIC/WALK_IN)

## API Endpoints

### 1. Manager Dashboard
```
GET /api/reports/dashboard/manager
Authorization: Bearer <token>
Headers:
  X-User-Role: MANAGER or RECEPTIONIST
  X-Hotel-Id: <hotelId>
```
**Response:**
```json
{
  "success": true,
  "message": "Manager dashboard retrieved successfully",
  "data": {
    "totalRevenue": 1250000,
    "todayRevenue": 85000,
    "totalBookings": 3420,
    "todayBookings": 22,
    "checkInsToday": 18,
    "checkOutsToday": 16,
    "availableRoomsToday": 24,
    "averageRating": 4.4,
    "todayRevenueByHour": [
      { "hour": 10, "amount": 18000 },
      { "hour": 11, "amount": 22000 },
      ...
    ],
    "todayBookingsBySource": [
      { "hour": 10, "publicBookings": 6, "walkInBookings": 3 },
      { "hour": 11, "publicBookings": 8, "walkInBookings": 2 },
      ...
    ]
  }
}
```
**Authorization**: Only MANAGER or RECEPTIONIST can access. Shows data for their hotel only.

### 2. Admin Dashboard
```
GET /api/reports/dashboard/admin
GET /api/reports/dashboard/admin?hotelId={hotelId}
Authorization: Bearer <token>
Headers:
  X-User-Role: ADMIN
```
**Response**: Same structure as Manager Dashboard
- Without `hotelId`: Shows aggregated data for ALL hotels
- With `hotelId`: Shows data for specific hotel

**Authorization**: Only ADMIN can access.

## Booking Source

Bookings are automatically tagged with a source:
- **PUBLIC**: When GUEST creates booking via system
- **WALK_IN**: When MANAGER/ADMIN creates booking (receptionist)

## Database Configuration

### application.properties
```properties
# Booking Database
spring.datasource.booking.url=jdbc:mysql://localhost:3306/hms_booking_db
spring.datasource.booking.username=root
spring.datasource.booking.password=Ish983556

# Billing Database
spring.datasource.billing.url=jdbc:mysql://localhost:3306/hms_billing_db
spring.datasource.billing.username=root
spring.datasource.billing.password=Ish983556

# Hotel Database
spring.datasource.hotel.url=jdbc:mysql://localhost:3306/hms_hotel_db
spring.datasource.hotel.username=root
spring.datasource.hotel.password=Ish983556
```

## Key Features

1. **Simple & Minimal**: No over-engineering, straightforward queries
2. **Read-Only**: No data modifications
3. **Role-Based Access**: ADMIN sees all, MANAGER sees only their hotel
4. **Direct Database Access**: No synchronous service calls
5. **Hourly Aggregation**: Revenue and bookings grouped by hour

## Implementation Details

### Data Sources Configuration
- Three separate `@Configuration` classes for each datasource
- Separate entity managers and transaction managers
- Repository packages separated by datasource

### Queries
- Simple JPQL queries with optional hotelId filtering
- Aggregation at database level (not in-memory)
- Efficient date-based filtering

### Response Structure
- Identical response structure for ADMIN and MANAGER
- All metrics included in single response
- Graphs include all 24 hours (0-23) with zero values where no data

## Future Enhancements

1. **Average Rating**: Currently hardcoded. Integrate with review/feedback service when available
2. **More Metrics**: Add occupancy rate, cancellation rate, etc.
3. **Date Range Filters**: Allow filtering by date range
4. **Export**: Add CSV/PDF export functionality

