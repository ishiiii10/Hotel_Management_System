# Hotel Management System

## 📋 Overview

A microservices-based Hotel Management System built with Spring Boot and Angular. The system enables hotel booking, billing, user management, and comprehensive reporting features.

## 🏗️ Architecture

- **Pattern**: Microservices with API Gateway
- **Service Discovery**: Netflix Eureka
- **Configuration**: Spring Cloud Config Server
- **Messaging**: Apache Kafka (Event-Driven)
- **Database**: MySQL (Separate DB per service)
- **Cache**: Redis
- **Authentication**: JWT




### Architecture Diagram
```
[Architecture Diagram Placeholder - Add SystemDesign.png here]
```

## 🎯 Services

| Service | Port | Description |
|---------|------|-------------|
| Eureka Service | 8761 | Service Discovery |
| Config Server | 8888 | Centralized Configuration |
| API Gateway | 8080 | Single Entry Point, Routing |
| Auth Service | 9001 | Authentication & Authorization |
| Hotel Service | 9002 | Hotel & Room Management |
| Booking Service | 9003 | Booking Management |
| Billing Service | 9005 | Bill Generation & Payments |
| Notification Service | 9004 | Email/SMS Notifications |
| Reports Service | 9006 | Dashboards & Analytics |

### Service Architecture Diagram
```
[Service Architecture Diagram Placeholder - Add ARCHITECTURE_DIAGRAM.md content visualization]
```

## 👥 User Roles

- **ADMIN**: Full system access
- **GUEST**: Can make bookings
- **MANAGER**: Hotel manager (bound to specific hotel)
- **RECEPTIONIST**: Front desk staff (bound to specific hotel)

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- Node.js 18+
- MySQL 8.0
- Redis
- Apache Kafka

### Backend Setup

1. **Start Infrastructure Services**:
   ```bash
   # Start MySQL, Redis, Kafka
   ```

2. **Start Services** (in order):
   ```bash
   # 1. Eureka Service
   cd HMS_Backend/eureka-service
   mvn spring-boot:run
   
   # 2. Config Server
   cd HMS_Backend/config-server
   mvn spring-boot:run
   
   # 3. Auth Service
   cd HMS_Backend/auth-service
   mvn spring-boot:run
   
   # 4. Hotel Service
   cd HMS_Backend/hotel-service
   mvn spring-boot:run
   
   # 5. Other services...
   ```

3. **Create Databases**:
   ```sql
   CREATE DATABASE hms_auth_db;
   CREATE DATABASE hms_hotel_db;
   CREATE DATABASE hms_booking_db;
   CREATE DATABASE hms_billing_db;
   ```

### Frontend Setup

```bash
cd hms-frontend
npm install
ng serve
```

## 📡 Key API Endpoints

### Auth Service
- `POST /auth/register/guest` - Guest registration
- `POST /auth/login` - User login
- `GET /auth/me` - Get current user

### Hotel Service
- `GET /hotels/search` - Search hotels (Public)
- `GET /hotels/{hotelId}` - Get hotel details (Public)
- `POST /hotels` - Create hotel (Admin only)

### Booking Service
- `GET /bookings/check-availability` - Check availability (Public)
- `POST /bookings` - Create booking (Protected)
- `GET /bookings/my-bookings` - Get user bookings

### Billing Service
- `GET /bills/booking/{bookingId}` - Get bill by booking
- `POST /bills/{billId}/mark-paid` - Mark bill as paid (Admin)

**Full API Documentation**: See `SOFTWARE_DESIGN_DOCUMENT.md`

## 🗄️ Database Schema

### Core Entities
- **User**: Authentication & user management
- **Hotel**: Hotel information
- **Room**: Room details per hotel
- **Booking**: Booking records
- **Bill**: Billing information
- **Payment**: Payment records

### ER Diagram
```
[ER Diagram Placeholder - Add ErDiagram.png here]
```

**Complete Schema**: See `SOFTWARE_DESIGN_DOCUMENT.md`

## 🔄 Key Flows

### Booking Flow
```
1. Guest searches hotels
2. Checks availability
3. Selects room & dates
4. Provides guest details
5. Booking created (status: CREATED)
6. Booking confirmed → Bill generated
7. Payment made → Booking confirmed
8. Check-in → Check-out
```

### Booking Flow Diagram
```
[Booking Flow Diagram Placeholder - Add BookingFlow.png here]
```

### Billing Flow
```
1. Booking confirmed
2. Kafka event: booking-confirmed
3. Billing Service generates bill (PENDING)
4. Admin marks bill as PAID
5. Payment record created
```

### Billing Flow Diagram
```
[Bill Generation Flow Diagram Placeholder - Add BillGenerateFlow.png here]
```

## 🔐 Security

- **JWT Authentication**: Token-based auth
- **Role-Based Access Control**: Admin, Guest, Manager, Receptionist
- **Context-Aware Authorization**: Staff bound to hotels
- **Password Encryption**: BCrypt

## 📊 Features

### Guest Features
- Search hotels by city/category
- View hotel details
- Check room availability
- Create bookings
- View booking history
- Make payments

### Staff Features
- Manage hotel rooms
- Handle check-in/check-out
- View hotel bookings
- Generate bills
- View dashboards

### Admin Features
- Manage all hotels
- Create staff users
- View system-wide reports
- Manage users

## 🛠️ Technology Stack

### Backend
- Spring Boot 3.x
- Spring Cloud (Gateway, Config, Eureka)
- Apache Kafka
- MySQL 8.0
- Redis
- JWT

### Frontend
- Angular
- TypeScript
- RxJS

## 📁 Project Structure

```
Hotel_Management_System/
├── HMS_Backend/
│   ├── api-gateway/
│   ├── auth-service/
│   ├── hotel-service/
│   ├── booking-service/
│   ├── billing-service/
│   ├── notification-service/
│   ├── reports-service/
│   ├── eureka-service/
│   ├── config-server/
│   └── docker-compose.yml
├── hms-frontend/
│   └── src/
└── README.md
```

## 🔧 Configuration

### Local Development
- Update `bootstrap.properties` in each service
- Set database URLs to `localhost:3306`
- Set Eureka URL to `http://localhost:8761/eureka`
- Set Config Server URL to `http://localhost:8888`
- Set Redis host to `localhost:6379`
- Set Kafka bootstrap servers to `localhost:9092`

### Important Configuration Files
- `bootstrap.properties` - Service configuration
- `application.properties` - Application settings
- `docker-compose.yml` - Docker setup

## 📈 Reports & Analytics

The Reports Service provides:
- Manager Dashboard (hotel-specific metrics)
- Admin Dashboard (system-wide metrics)
- Revenue tracking
- Booking analytics
- Occupancy rates

### Reports Dashboard Screenshots
```
[Reports Dashboard Placeholder - Add dashboard screenshots]
```

## 🧪 Testing

### Test Coverage
- Unit tests for all services
- Integration tests
- Service coverage reports in `JacocoReports/`

### Coverage Reports
```
[Coverage Reports Placeholder - Add JacocoReports images]
```

## 📚 Documentation

- **Software Design Document**: `HMS_Backend/SOFTWARE_DESIGN_DOCUMENT.md`
- **Service READMEs**: Individual service documentation
- **API Documentation**: Postman collection available

## 🔄 Event Flow

### Kafka Events
- `booking-created` → Notification Service
- `booking-confirmed` → Billing Service, Notification Service
- `guest-checked-in` → Notification Service
- `checkout-completed` → Notification Service

### Event Flow Diagrams
```
[Notification Flow Diagram Placeholder - Add NotificationFlow.png here]
```

## 📸 UI Screenshots

### Landing Page
```
[Landing Page Screenshot Placeholder]
```

### Hotel List Page
```
[Hotel List Page Screenshot Placeholder]
```

### Hotel Details Page
```
[Hotel Details Page Screenshot Placeholder]
```

### Booking Modal
```
[Booking Modal Screenshot Placeholder]
```

### Dashboard
```
[Dashboard Screenshot Placeholder]
```

## 🚀 Deployment

### Docker Deployment
```bash
docker-compose up -d
```

### Individual Service Deployment
- Each service has its own Dockerfile
- Use `docker-compose.yml` for orchestration

## 📞 Contact & Support

- **Company**: Chubb
- **Email**: ishiii25arya@gmail.com
- **Phone**: 8340457343

## 📝 License

© 2024 BELLE VUE | Chubb | All rights reserved

## 🎯 Quick Links

- [Software Design Document](./HMS_Backend/SOFTWARE_DESIGN_DOCUMENT.md)
- [Architecture Diagram](./HMS_Backend/ARCHITECTURE_DIAGRAM.md)
- [Service READMEs](./HMS_Backend/)

---

**Last Updated**: January 2026

