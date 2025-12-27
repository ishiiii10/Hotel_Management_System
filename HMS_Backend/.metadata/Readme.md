# Hotel Booking Platform – Microservices Backend

A **microservices-based hotel booking platform** designed to model real-world reservation workflows, role-based access control, and event-driven communication between services.

This project focuses on **backend architecture**, clean service boundaries, and scalable system design using **Spring Boot, OpenFeign, and Apache Kafka**.

---

## Project Overview

The platform is composed of independently deployable backend services.  
Each service owns its data and business logic and communicates via REST or events.

### Core Services

- **Auth Service**
  - Handles authentication and authorization
  - Issues JWT tokens
  - Manages user roles and hotel assignments

- **API Gateway**
  - Single entry point for all client requests
  - Routes requests to downstream services
  - Validates authentication tokens

- **Service Discovery (Eureka)**
  - Registers and discovers microservices dynamically
  - Enables loose coupling between services

- **Hotel Service**
  - Manages hotel metadata
  - Handles room inventory, room categories, pricing, and availability
  - Reacts to room state change events

- **Booking Service**
  - Core reservation engine
  - Handles booking creation, modification, and cancellation
  - Manages booking lifecycle and state transitions
  - Prevents double booking

- **Billing Service**
  - Generates invoices and manages payments
  - Handles refunds
  - Reacts to booking-related events

- **Notification Service**
  - Sends email notifications for bookings, check-ins, check-outs, and payments
  - Consumes events asynchronously
  - Designed to be non-blocking and fault-tolerant

---

## Architecture Overview

The system follows a **microservices + event-driven architecture**.

### Synchronous Communication
- **OpenFeign**
- Used primarily between Booking Service and Hotel Service
- Used only for read-based operations (availability, metadata)

### Asynchronous Communication
- **Apache Kafka**
- Used for domain events such as:
  - Booking created / cancelled
  - Check-in / check-out completed
  - Room released or booked
  - Payment completed

Each service has its **own database schema**.  
No service directly accesses another service’s database.

---

## User Roles

The platform supports four non-overlapping roles:

### Global Admin
- System-wide privileges
- Creates and registers hotels
- Assigns Hotel Managers and Receptionists
- Cannot be overridden by any other role

### Hotel Manager
- Assigned to one or more hotels
- Can manage:
  - Room types
  - Pricing
  - Availability
- Access strictly limited to assigned hotels

### Receptionist
- Assigned to specific hotels
- Handles:
  - Guest check-in and check-out
  - Walk-in bookings
- Cannot modify hotel metadata or pricing

### Guest
- End user of the platform
- Can:
  - Search hotels and rooms
  - Create bookings
  - Update or cancel own bookings
- No administrative access

---

## Booking Lifecycle

A booking follows a controlled state machine:
BOOKED → CONFIRMED → CHECKED_IN → CHECKED_OUT
BOOKED → CANCELLED

Invalid state transitions are rejected at the service layer.

Room state changes are propagated asynchronously using Kafka events.

---

## Technology Stack

### Backend
- Java 17+
- Spring Boot
- Spring Security (JWT)
- Spring Cloud Gateway
- Spring Cloud OpenFeign
- Spring Cloud Netflix Eureka
- Spring Data JPA
- Apache Kafka

### Databases
- Relational database per service (PostgreSQL / MySQL)

---

## Key Design Principles

- Clear service ownership and responsibility boundaries
- Role-based access control enforced at API level
- No shared databases across services
- Event-driven communication for state propagation
- Synchronous calls limited to read-only operations
- Failure isolation between critical and non-critical services

---

## Current Status

This repository represents the **initial backend architecture setup**.

Planned phases:
- Service scaffolding
- Database schema definition
- REST API contract design
- Kafka event schema definition
- Business logic implementation
- Testing and documentation

---

## Future Enhancements

- Distributed tracing and centralized logging
- Idempotency handling for critical operations
- Retry and dead-letter handling for Kafka consumers
- API versioning
- CI/CD pipeline setup

---

## Disclaimer

This project is built for **learning and architectural demonstration purposes**.  
It models real-world backend patterns but is not production-ready.

---

## Author

Backend-focused microservices project using Spring Boot, OpenFeign, and Kafka.