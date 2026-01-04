# Auth Service Implementation

## Overview
The Auth Service is the central authentication and authorization service for the Hotel Management System. It manages user accounts, authentication, JWT token generation, and user-hotel bindings.

## Core Responsibilities

1. **User Management**: Create and manage user accounts (GUEST, MANAGER, RECEPTIONIST, ADMIN)
2. **Authentication**: Login and JWT token generation
3. **Authorization**: Role-based access control with context-aware hotel binding
4. **Staff Creation**: Create staff users bound to specific hotels (via Hotel Service)
5. **User Activation**: Account activation via activation tokens

## Architecture

### User Roles

- **ADMIN**: System administrator, can access all hotels
- **GUEST**: Regular users who can make bookings
- **MANAGER**: Hotel manager, bound to a specific hotel
- **RECEPTIONIST**: Front desk staff, bound to a specific hotel

### User-Hotel Binding

- **ADMIN & GUEST**: `hotelId` is `null` (not bound to any hotel)
- **MANAGER & RECEPTIONIST**: `hotelId` is **required** (must be bound to a hotel)
- Hotel binding is enforced at user creation time
- JWT tokens include `hotelId` for staff users

### JWT Token Structure

JWT tokens include:
- `userId`: Internal user ID
- `username`: Unique username
- `publicUserId`: Public-facing user identifier
- `email`: User email
- `role`: User role (ADMIN, GUEST, MANAGER, RECEPTIONIST)
- `hotelId`: Hotel ID (only for MANAGER/RECEPTIONIST)

## API Endpoints

### Public Endpoints

#### 1. Guest Registration
```
POST /auth/register/guest
Content-Type: application/json
```
**Request Body:**
```json
{
  "fullName": "John Doe",
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

**Response:**
```json
{
  "id": 1,
  "publicUserId": "550e8400-e29b-41d4-a716-446655440000",
  "username": "johndoe",
  "fullName": "John Doe",
  "email": "john@example.com",
  "role": "GUEST",
  "enabled": true
}
```

#### 2. Login
```
POST /auth/login
Content-Type: application/json
```
**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "role": "GUEST",
  "hotelId": null
}
```

**Note:** For MANAGER/RECEPTIONIST, `hotelId` will be included in the response.

### Protected Endpoints

#### 3. Get Current User
```
GET /auth/me
Authorization: Bearer <token>
```

**Response:**
```json
{
  "id": 1,
  "publicUserId": "550e8400-e29b-41d4-a716-446655440000",
  "username": "johndoe",
  "fullName": "John Doe",
  "email": "john@example.com",
  "role": "GUEST",
  "enabled": true
}
```

#### 4. Change Password
```
POST /auth/change-password
Authorization: Bearer <token>
Content-Type: application/json
```
**Request Body:**
```json
{
  "currentPassword": "OldPass123!",
  "newPassword": "NewPass123!"
}
```

**Response:** `204 No Content`

### Admin Endpoints

#### 5. List All Users
```
GET /auth/admin/users
Authorization: Bearer <admin-token>
```

**Response:**
```json
[
  {
    "id": 1,
    "publicUserId": "550e8400-e29b-41d4-a716-446655440000",
    "username": "johndoe",
    "fullName": "John Doe",
    "email": "john@example.com",
    "role": "GUEST",
    "enabled": true,
    "hotelId": null
  },
  {
    "id": 2,
    "publicUserId": "...",
    "username": "manager1",
    "fullName": "Jane Manager",
    "email": "jane@hotel.com",
    "role": "MANAGER",
    "enabled": true,
    "hotelId": 5
  }
]
```

#### 6. Deactivate User
```
PATCH /auth/admin/users/{userId}/deactivate
Authorization: Bearer <admin-token>
```

**Response:** `204 No Content`

#### 7. Reassign Staff to Hotel
```
PUT /auth/admin/staff/{userId}/hotel-allotment?hotelId={hotelId}
Authorization: Bearer <admin-token>
```

**Response:** `204 No Content`

### Internal Endpoints (Feign)

#### 8. Get User by ID
```
GET /internal/auth/users/{userId}
```
**Used by:** Notification Service, other services
**Returns:** Raw user data without wrapper

## Staff Creation Flow

### Via Hotel Service (Recommended)

1. **ADMIN calls Hotel Service:**
   ```
   POST /hotels/{hotelId}/staff
   ```

2. **Hotel Service validates:**
   - Hotel exists
   - User is ADMIN

3. **Hotel Service calls Auth Service via Feign:**
   ```
   POST /auth/admin/staff
   Body: {
     "fullName": "Jane Manager",
     "username": "jane_manager",
     "email": "jane@hotel.com",
     "password": "SecurePass123!",
     "role": "MANAGER",
     "hotelId": 5
   }
   ```

4. **Auth Service creates user:**
   - Validates role (must be MANAGER or RECEPTIONIST)
   - Enforces `hotelId` is not null
   - Creates user with hotel binding
   - Returns activation token

**Response:**
```json
{
  "activationToken": "abc123def456..."
}
```

## User Entity

### Fields

- `id`: Primary key
- `publicUserId`: UUID for public-facing operations
- `username`: Unique username (3-50 chars, alphanumeric + underscore)
- `fullName`: User's full name
- `email`: Unique email address
- `password`: Encrypted password (BCrypt)
- `role`: User role (ADMIN, GUEST, MANAGER, RECEPTIONIST)
- `hotelId`: Hotel ID (null for ADMIN/GUEST, required for MANAGER/RECEPTIONIST)
- `enabled`: Account enabled status
- `createdAt`: Account creation timestamp
- `updatedAt`: Last update timestamp

### Constraints

- `email`: Unique, required
- `username`: Unique, required, 3-50 characters, alphanumeric + underscore only
- `hotelId`: Required for MANAGER/RECEPTIONIST, null for ADMIN/GUEST

## Security Features

### Password Encryption
- Passwords are encrypted using BCrypt
- Never stored in plain text

### JWT Tokens
- Tokens include user identity and role
- Tokens include `hotelId` for staff users
- Configurable expiry (default: 60 minutes)
- Secret key configurable via `auth.jwt.secret`

### Context-Aware Authorization
- Staff users are bound to specific hotels
- JWT includes `hotelId` for staff
- API Gateway extracts and forwards `hotelId` in headers
- Downstream services enforce hotel-specific access

## Database Setup

Create the database:
```sql
CREATE DATABASE hms_auth_db;
```

## Configuration

### application.properties
```properties
spring.application.name=auth-service
server.port=9001

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/hms_auth_db
spring.datasource.username=root
spring.datasource.password=Ish983556

# JWT Configuration
auth.jwt.secret=super-secret-key-change-later-1234567890
auth.jwt.expiry-minutes=60

# Admin User (auto-created on startup)
auth.admin.email=admin@hotelbooking.com
auth.admin.password=admin123
auth.admin.full-name=System Admin
```

## Key Features

1. **Automatic Admin Creation**: Admin user is created on first startup
2. **Activation Tokens**: Staff users receive activation tokens (for future email activation)
3. **Hotel Binding**: Staff users are bound to hotels at creation time
4. **Public User ID**: UUID-based public identifier for external systems
5. **Username Support**: Unique username for consistent identity

## Integration Points

### With Hotel Service
- Hotel Service calls Auth Service via Feign to create staff
- Staff users are created with hotel binding

### With API Gateway
- API Gateway validates JWT tokens
- Extracts user info and adds headers: `X-User-Id`, `X-User-Role`, `X-Hotel-Id`, `X-User-Username`, `X-User-Public-Id`

### With Notification Service
- Notification Service fetches user details via Feign
- Uses `/internal/auth/users/{userId}` endpoint

## Error Handling

Common errors:
- `InvalidCredentialsException`: Wrong email/password
- `AccountDisabledException`: Account is disabled
- `IllegalArgumentException`: Invalid role or missing hotelId for staff
- `IllegalStateException`: User not found, validation failures

## Testing

### Test Login Flow
1. Register a guest: `POST /auth/register/guest`
2. Login: `POST /auth/login`
3. Use token in subsequent requests

### Test Staff Creation
1. Login as ADMIN
2. Create hotel: `POST /hotels`
3. Create staff: `POST /hotels/{hotelId}/staff`
4. Staff user is created with hotel binding

## Security Best Practices

1. **Change Default Admin Password**: Update `auth.admin.password` in production
2. **Use Strong JWT Secret**: Change `auth.jwt.secret` to a strong random string
3. **Enable HTTPS**: Use HTTPS in production
4. **Password Policy**: Enforce strong password requirements
5. **Token Expiry**: Adjust `auth.jwt.expiry-minutes` based on security requirements

