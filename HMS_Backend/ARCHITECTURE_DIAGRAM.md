# Hotel Management System - Simple Architecture Diagram

Simple high-level architecture showing frontend and backend integration.

```mermaid
graph TB
    subgraph "Frontend"
        WEB[Web App]
        MOBILE[Mobile App]
    end
    
    subgraph "Backend"
        GATEWAY[API Gateway]
        
        subgraph "Services"
            AUTH[Auth Service]
            HOTEL[Hotel Service]
            BOOKING[Booking Service]
            BILLING[Billing Service]
            NOTIFICATION[Notification Service]
        end
        
        KAFKA[Kafka<br/>Message Queue]
    end
    
    subgraph "Data"
        DB[(Databases)]
    end
    
    %% Main Flow
    WEB --> GATEWAY
    MOBILE --> GATEWAY
    GATEWAY --> AUTH
    GATEWAY --> HOTEL
    GATEWAY --> BOOKING
    GATEWAY --> BILLING
    
    %% Service Communication
    BOOKING --> HOTEL
    BOOKING --> KAFKA
    KAFKA --> BILLING
    KAFKA --> NOTIFICATION
    
    %% Data Layer
    AUTH --> DB
    HOTEL --> DB
    BOOKING --> DB
    BILLING --> DB
    NOTIFICATION --> DB
    
    %% Styling
    classDef frontend fill:#e3f2fd,stroke:#1976d2,stroke-width:2px
    classDef gateway fill:#fff3e0,stroke:#f57c00,stroke-width:2px
    classDef service fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px
    classDef queue fill:#fce4ec,stroke:#c2185b,stroke-width:2px
    classDef database fill:#fff9c4,stroke:#f9a825,stroke-width:2px
    
    class WEB,MOBILE frontend
    class GATEWAY gateway
    class AUTH,HOTEL,BOOKING,BILLING,NOTIFICATION service
    class KAFKA queue
    class DB database
```

---

## Simple Flow Explanation

### 1. **Frontend** → User Interface (Web/Mobile Apps)
   - Users interact with the application

### 2. **API Gateway** → Single Entry Point
   - Receives all requests from frontend
   - Routes to appropriate services

### 3. **Services** → Business Logic
   - **Auth Service**: Login, Registration
   - **Hotel Service**: Hotels & Rooms
   - **Booking Service**: Bookings & Availability
   - **Billing Service**: Bills & Payments
   - **Notification Service**: Emails & SMS

### 4. **Kafka** → Event Messaging
   - Booking Service sends events
   - Billing & Notification Services listen to events

### 5. **Databases** → Data Storage
   - Each service stores its own data

---

## Key Features

✅ **Simple & Clean**: Easy to understand architecture  
✅ **Microservices**: Independent services  
✅ **Event-Driven**: Asynchronous processing via Kafka  
✅ **Scalable**: Each component can scale independently  
✅ **Secure**: API Gateway handles authentication

