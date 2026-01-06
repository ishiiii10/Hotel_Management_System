# Prompt for Documentation Tools (Notion, Confluence, ChatGPT, etc.)

Use this prompt in your preferred documentation tool to generate a formatted Software Design Document:

---

## PROMPT START

Create a comprehensive Software Design Document for a Hotel Management System with the following structure and content:

# Hotel Management System - Software Design Document

## 1. EXECUTIVE SUMMARY
- Microservices-based hotel management system
- Supports multiple user roles: Admin, Guest, Manager, Receptionist
- Event-driven architecture using Apache Kafka
- JWT-based authentication and role-based access control

## 2. SYSTEM ARCHITECTURE

### 2.1 Services Overview
List all 9 services with their ports and responsibilities:
1. Eureka Service (8761) - Service discovery
2. Config Server (8888) - Centralized configuration
3. API Gateway (8080) - Single entry point
4. Auth Service (9001) - Authentication & authorization
5. Hotel Service (9002) - Hotel & room management
6. Booking Service (9003) - Booking management
7. Billing Service (9005) - Bill generation & payments
8. Notification Service (9004) - Email/SMS notifications
9. Reports Service (9006) - Dashboards & analytics

### 2.2 Technology Stack
- Spring Boot 3.x, Java, Maven
- Netflix Eureka (Service Discovery)
- Spring Cloud Gateway (API Gateway)
- Apache Kafka (Event Messaging)
- MySQL 8.0 (Databases)
- Redis (Caching)
- JWT (Authentication)

## 3. USER ROLES & ACCESS CONTROL

### 3.1 Roles
- **ADMIN**: Full system access, not bound to hotels
- **GUEST**: Can make bookings, not bound to hotels
- **MANAGER**: Hotel manager, bound to specific hotel
- **RECEPTIONIST**: Front desk staff, bound to specific hotel

### 3.2 Access Control Rules
- Staff users (MANAGER/RECEPTIONIST) can only access their assigned hotel
- JWT tokens include hotelId for staff users
- API Gateway injects user context headers

## 4. API ENDPOINTS DOCUMENTATION

Create detailed tables for each service with:
- HTTP Method
- Endpoint Path
- Description
- Access Control (Who can access)
- Request/Response examples (optional)

### 4.1 Auth Service Endpoints
[Include all endpoints from the full document - see SOFTWARE_DESIGN_DOCUMENT.md]

### 4.2 Hotel Service Endpoints
[Include all endpoints]

### 4.3 Booking Service Endpoints
[Include all endpoints]

### 4.4 Billing Service Endpoints
[Include all endpoints]

### 4.5 Reports Service Endpoints
[Include all endpoints]

## 5. DATABASE SCHEMA

### 5.1 Auth Service Database (hms_auth_db)
- User table schema with all fields, types, constraints
- UserHotelAssignment table
- ActivationToken table

### 5.2 Hotel Service Database (hms_hotel_db)
- Hotel table schema
- Room table schema
- RoomAvailability table schema

### 5.3 Booking Service Database (hms_booking_db)
- Booking table schema
- ScheduledReminder table schema

### 5.4 Billing Service Database (hms_billing_db)
- Bill table schema
- Payment table schema

Include:
- Primary keys (PK)
- Foreign keys (FK)
- Unique constraints (UK)
- Enums and their values
- Data types
- Default values

## 6. BUSINESS RULES

Document all business rules organized by domain:

### 6.1 User Management Rules
- Guest self-registration
- Staff creation (ADMIN only)
- Hotel binding requirements
- Password encryption

### 6.2 Hotel Management Rules
- Hotel creation (ADMIN only)
- Room uniqueness within hotel
- Soft delete for rooms
- Room status management

### 6.3 Booking Management Rules
- Booking status flow
- Availability checking
- Double booking prevention
- Check-in/check-out rules
- Cancellation rules

### 6.4 Billing & Payment Rules
- Automatic bill generation
- One booking = one bill
- Payment authorization
- Append-only payment records

### 6.5 Authorization Rules
- Context-aware access control
- Resource ownership
- Hotel-scoped operations

## 7. INTEGRATION POINTS

### 7.1 Service-to-Service Communication
- Feign clients for synchronous calls
- List all service interactions

### 7.2 Event-Driven Communication
- Kafka topics and events
- Event consumers and producers

### 7.3 API Gateway
- Routing rules
- Authentication flow
- Header injection

## 8. ERROR HANDLING

- Common HTTP status codes
- Error response format
- Exception handling strategy

## 9. DEPLOYMENT

- Service ports
- Database ports
- External service ports (Kafka, Redis)

## 10. APPENDIX

- All enum values
- Glossary of terms
- Reference documentation

---

**Formatting Requirements:**
- Use tables for API endpoints
- Use code blocks for database schemas
- Use bullet points for business rules
- Include visual diagrams if possible (architecture, data flow)
- Make it searchable and well-indexed

**Style:**
- Professional technical documentation
- Clear and concise
- Well-organized with proper headings
- Include examples where helpful

---

## PROMPT END

---

## HOW TO USE THIS PROMPT

### Option 1: Notion
1. Create a new page in Notion
2. Paste the prompt above
3. Use Notion AI (if available) or manually format
4. Copy content from SOFTWARE_DESIGN_DOCUMENT.md and format in Notion

### Option 2: Confluence
1. Create a new page
2. Use the prompt with Confluence's AI assistant
3. Or import the markdown file and format

### Option 3: ChatGPT/Claude
1. Copy the prompt
2. Paste into ChatGPT/Claude
3. Ask it to format as a professional Software Design Document
4. Copy the output to your documentation tool

### Option 4: Direct Import
1. Open SOFTWARE_DESIGN_DOCUMENT.md
2. Copy sections as needed
3. Paste into your documentation tool
4. Format according to tool's capabilities

---

## RECOMMENDED TOOLS

1. **Notion** - Best for collaborative documentation, great formatting options
2. **Confluence** - Enterprise-grade, good for teams
3. **GitBook** - Developer-friendly, markdown-based
4. **Markdown files** - Simple, version-controlled
5. **Google Docs** - Easy collaboration, basic formatting

---

## TIPS FOR FORMATTING

1. **Use Tables**: API endpoints work best in tables
2. **Code Blocks**: Database schemas should be in code blocks
3. **Diagrams**: Add architecture diagrams using Mermaid or draw.io
4. **Searchability**: Use proper headings for easy navigation
5. **Version Control**: Keep track of document versions
6. **Examples**: Include request/response examples for APIs
7. **Visual Hierarchy**: Use proper heading levels (H1, H2, H3)

---

Good luck creating your documentation! 🚀

