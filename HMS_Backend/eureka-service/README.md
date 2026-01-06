# Eureka Service Discovery

## What This Does

This is the service discovery server. All microservices register themselves here, and other services can find them through Eureka. It's like a phone book for microservices.

## How It Works

1. When a microservice starts, it registers itself with Eureka
2. Eureka keeps track of all registered services
3. When services need to talk to each other, they look up the service name in Eureka
4. Eureka returns the actual URL where the service is running

## Configuration

### application.properties

```properties
spring.application.name=eureka-service
server.port=8761

# Eureka Server Configuration
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

The `register-with-eureka=false` means Eureka itself doesn't register with itself (it's the server, not a client).

## Accessing Eureka Dashboard

Once the service is running, you can access the dashboard at:
```
http://localhost:8761
```

The dashboard shows:
- All registered services
- Service instances
- Health status
- Metadata

## Service Registration

Each microservice needs to be configured to register with Eureka:

```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka
```

When a service starts, it will:
1. Connect to Eureka
2. Register itself with a service name (like AUTH-SERVICE, HOTEL-SERVICE)
3. Send heartbeats to stay registered
4. If it stops, Eureka removes it after a timeout

## Service Discovery

Services can discover each other using:
- Service name (like `AUTH-SERVICE`) instead of hardcoded URLs
- Feign clients automatically use Eureka for service discovery
- Load balancing is handled automatically (if multiple instances)

## Important Notes

- Eureka must be started first before other services
- Services won't be able to find each other if Eureka is down
- The dashboard is useful for debugging (see which services are registered)
- Service names are usually uppercase (AUTH-SERVICE, not auth-service)
- Eureka handles load balancing if you have multiple instances of a service

## Common Issues

- Service not registering: Check if Eureka URL is correct in service config
- Service not found: Make sure Eureka is running and service is registered
- Connection refused: Check if Eureka is running on port 8761

## Testing

1. Start Eureka service
2. Open http://localhost:8761 in browser
3. Start other microservices
4. Check dashboard - services should appear in "Instances currently registered with Eureka"

