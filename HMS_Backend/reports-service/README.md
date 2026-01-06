# Reports Service

## Overview

This service provides dashboards for ADMIN and MANAGER roles. It's a read-only service that just reads data from booking, billing, and hotel databases. It doesn't modify anything, just shows metrics and graphs.

## Architecture

### Multiple Data Sources
The service connects to three different databases:
- **Booking Database** - For booking data
- **Billing Database** - For revenue and payment data  
- **Hotel Database** - For room availability data

### Read-Only Design
- No data modifications
- No synchronous service calls to other services
- Just simple JPQL queries
- Direct database access

## Dashboard Metrics

### Numbers (Metrics)
1. **Total Revenue** - Sum of all PAID bills (all-time)
2. **Today Revenue** - Sum of PAID bills created today
3. **Total Bookings** - Total confirmed bookings (all-time)
4. **Today Bookings** - Bookings created today
5. **Check-ins Today** - Bookings with checkInDate = today
6. **Check-outs Today** - Bookings with checkOutDate = today
7. **Available Rooms Today** - Count of available rooms for today
8. **Average Rating** - Currently hardcoded to 4.4 (will integrate with review service later)

### Graphs
1. **Today Revenue by Hour** - Revenue amount for each hour (0-23)
2. **Today Bookings by Source** - Bookings grouped by hour and source (PUBLIC/WALK_IN)

## API Endpoints

### Manager Dashboard
```
GET /api/reports/dashboard/manager
```
Need to send:
- Authorization header with token
- X-User-Role: MANAGER or RECEPTIONIST
- X-Hotel-Id: <hotelId>

Shows data only for their hotel.

### Admin Dashboard
```
GET /api/reports/dashboard/admin
GET /api/reports/dashboard/admin?hotelId={hotelId}
```
- Without hotelId: Shows aggregated data for ALL hotels
- With hotelId: Shows data for specific hotel
- Only ADMIN can access

## Booking Source

Bookings are tagged with a source:
- **PUBLIC** - When GUEST creates booking via system
- **WALK_IN** - When MANAGER/ADMIN creates booking (receptionist)

## Database Configuration

You need to configure three datasources in `application.properties`:

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

1. **Simple & Minimal** - No over-engineering, just straightforward queries
2. **Read-Only** - Doesn't modify any data
3. **Role-Based Access** - ADMIN sees all, MANAGER sees only their hotel
4. **Direct Database Access** - No synchronous service calls (faster)
5. **Hourly Aggregation** - Revenue and bookings grouped by hour

## Implementation Details

### Data Sources Configuration
- Three separate `@Configuration` classes for each datasource
- Separate entity managers and transaction managers
- Repository packages separated by datasource

### Queries
- Simple JPQL queries with optional hotelId filtering
- Aggregation at database level (not in-memory, more efficient)
- Efficient date-based filtering

### Response Structure
- Same response structure for ADMIN and MANAGER
- All metrics included in single response
- Graphs include all 24 hours (0-23) with zero values where no data

## Future Enhancements

1. **Average Rating** - Currently hardcoded, will integrate with review service
2. **More Metrics** - Add occupancy rate, cancellation rate, etc.
3. **Date Range Filters** - Allow filtering by date range
4. **Export** - Add CSV/PDF export functionality

## Important Notes

- This is a read-only service, it doesn't change any data
- Connects directly to databases (no service calls)
- ADMIN can see all hotels or filter by hotelId
- MANAGER can only see their hotel
- Average rating is currently hardcoded (4.4)

