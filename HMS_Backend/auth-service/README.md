# Auth Service

## What This Service Does

This is the authentication and authorization service for our Hotel Management System. Basically, it handles all the user stuff - login, registration, creating accounts, and making sure people can only access what they're supposed to.

## Main Features

1. **User Management** - We can create users with different roles (GUEST, MANAGER, RECEPTIONIST, ADMIN)
2. **Login & Authentication** - Users can login and get JWT tokens
3. **Authorization** - Different roles have different permissions
4. **Staff Creation** - We can create staff users that are bound to specific hotels
5. **User Activation** - There's an activation system for new accounts

## User Roles

We have 4 types of users:
- **ADMIN** - Can do everything, access all hotels
- **GUEST** - Regular users who book hotels
- **MANAGER** - Hotel managers, they're assigned to one specific hotel
- **RECEPTIONIST** - Front desk staff, also assigned to one hotel

## How Hotel Binding Works

So here's the thing - ADMIN and GUEST users don't belong to any hotel (hotelId is null). But MANAGER and RECEPTIONIST must be assigned to a hotel when they're created. This hotelId gets included in their JWT token so other services know which hotel they work for.

## JWT Token Structure

When a user logs in, they get a JWT token that contains:
- userId (internal ID)
- username
- publicUserId (UUID for external use)
- email
- role (ADMIN, GUEST, etc.)
- hotelId (only for MANAGER/RECEPTIONIST)

## API Endpoints

### Public Endpoints

#### Register as Guest
```
POST /auth/register/guest
```
You need to send:
```json
{
  "fullName": "John Doe",
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

#### Login
```
POST /auth/login
```
Send email and password, get back a token.

### Protected Endpoints

#### Get My Info
```
GET /auth/me
```
Need to send the JWT token in Authorization header.

#### Change Password
```
POST /auth/change-password
```
Send current password and new password.

### Admin Only Endpoints

#### List All Users
```
GET /auth/admin/users
```
Only admins can see this.

#### Deactivate User
```
PATCH /auth/admin/users/{userId}/deactivate
```

#### Reassign Staff to Hotel
```
PUT /auth/admin/staff/{userId}/hotel-allotment?hotelId={hotelId}
```

## How Staff Creation Works

The recommended way is through Hotel Service:
1. Admin calls Hotel Service to create staff
2. Hotel Service validates the hotel exists
3. Hotel Service calls Auth Service via Feign
4. Auth Service creates the user with hotel binding
5. Returns an activation token

## Database Setup

You need to create the database first:
```sql
CREATE DATABASE hms_auth_db;
```

## Configuration

In `application.properties`:
```properties
spring.application.name=auth-service
server.port=9001

# Database connection
spring.datasource.url=jdbc:mysql://localhost:3306/hms_auth_db
spring.datasource.username=root
spring.datasource.password=Ish983556

# JWT settings
auth.jwt.secret=super-secret-key-change-later-1234567890
auth.jwt.expiry-minutes=60

# Admin user (created automatically on startup)
auth.admin.email=admin@hotelbooking.com
auth.admin.password=admin123
auth.admin.full-name=System Admin
```

## Security Stuff

- Passwords are encrypted with BCrypt (never stored in plain text)
- JWT tokens expire after 60 minutes (configurable)
- Staff users have hotelId in their token for authorization
- API Gateway extracts user info from token and adds headers

## Integration with Other Services

- **Hotel Service** - Creates staff users via Feign
- **API Gateway** - Validates tokens and extracts user info
- **Notification Service** - Fetches user details for sending emails

## Common Errors

- `InvalidCredentialsException` - Wrong email/password
- `AccountDisabledException` - Account is disabled
- `IllegalArgumentException` - Invalid role or missing hotelId
- `IllegalStateException` - User not found, validation failures

## Testing

To test the login flow:
1. Register a guest: `POST /auth/register/guest`
2. Login: `POST /auth/login`
3. Use the token in Authorization header for other requests

To test staff creation:
1. Login as ADMIN
2. Create hotel via Hotel Service
3. Create staff: `POST /hotels/{hotelId}/staff`
4. Staff user gets created with hotel binding

## Important Notes

- Change the default admin password in production!
- Use a strong JWT secret (not the default one)
- Enable HTTPS in production
- The admin user is created automatically on first startup

