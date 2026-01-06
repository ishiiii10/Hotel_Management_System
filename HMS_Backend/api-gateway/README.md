# API Gateway

## What This Does

This is the entry point for all client requests. It routes requests to the appropriate microservices and handles authentication. All requests go through this gateway first.

## Architecture

The gateway uses Spring Cloud Gateway and routes requests based on path patterns. It also:
- Validates JWT tokens
- Extracts user information from tokens
- Adds headers (X-User-Id, X-User-Role, X-Hotel-Id, etc.) to downstream services
- Routes to services via Eureka service discovery

## Routes

The gateway has routes configured for:
- `/eureka/**` → Eureka Service
- `/auth/**` → Auth Service
- `/hotels/**` → Hotel Service
- `/bookings/**` → Booking Service
- `/bills/**` → Billing Service
- `/api/reports/**` → Reports Service

## Configuration

### application.properties

```properties
spring.application.name=api-gateway
server.port=9090

# Eureka
eureka.client.service-url.defaultZone=http://localhost:8761/eureka

# Gateway Routes
spring.cloud.gateway.routes[0].id=eureka-service-route
spring.cloud.gateway.routes[0].uri=lb://EUREKA-SERVICE
spring.cloud.gateway.routes[0].predicates[0]=Path=/eureka/**

spring.cloud.gateway.routes[1].id=auth-service-route
spring.cloud.gateway.routes[1].uri=lb://AUTH-SERVICE
spring.cloud.gateway.routes[1].predicates[0]=Path=/auth/**

# ... and so on for other services
```

## How It Works

1. Client sends request to gateway (port 9090)
2. Gateway checks if route matches a pattern
3. If protected route, validates JWT token
4. Extracts user info from token
5. Adds headers (X-User-Id, X-User-Role, X-Hotel-Id, etc.)
6. Routes to appropriate service via Eureka
7. Returns response to client

## Authentication

The gateway validates JWT tokens for protected routes. It:
- Extracts token from Authorization header
- Validates token signature
- Extracts user information
- Adds headers for downstream services

## Headers Added to Requests

When routing to downstream services, the gateway adds:
- `X-User-Id` - User's internal ID
- `X-User-Role` - User's role (ADMIN, GUEST, etc.)
- `X-Hotel-Id` - Hotel ID (for staff users)
- `X-User-Username` - Username
- `X-User-Public-Id` - Public user ID (UUID)

## Public vs Protected Routes

Some routes are public (no auth required):
- `/auth/register/**` - Registration endpoints
- `/auth/login` - Login endpoint
- `/hotels/search` - Hotel search
- `/hotels/{hotelId}` - Hotel details
- `/bookings/check-availability` - Availability check

Other routes require authentication.

## Error Handling

- Invalid tokens return 401 Unauthorized
- Missing tokens on protected routes return 401
- Service not found returns 503 Service Unavailable
- Invalid routes return 404 Not Found

## Important Notes

- This is the single entry point for all client requests
- All services should be accessed through the gateway
- Gateway uses Eureka for service discovery
- Port 9090 is the main port clients connect to
- JWT validation happens here before routing

## Testing

To test:
1. Start Eureka first
2. Start all microservices
3. Start API Gateway
4. Send requests to `http://localhost:9090` instead of individual service ports
5. For protected routes, include JWT token in Authorization header

