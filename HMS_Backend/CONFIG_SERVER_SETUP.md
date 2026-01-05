# Config Server Setup Guide

## Overview
The Hotel Management System uses Spring Cloud Config Server to centralize configuration management. All microservice configurations are stored in a GitHub repository and fetched at runtime.

## GitHub Repository Structure

The config server reads from: `https://github.com/ishiiii10/Hotel_Management_System_ConfigServer`

### Required File Structure in GitHub Repo:

```
Hotel_Management_System_ConfigServer/
├── billing-service.properties
├── booking-service.properties
├── auth-service.properties
├── hotel-service.properties
├── notification-service.properties
├── reports-service.properties
└── api-gateway.properties
```

## Configuration Files Content

Each service's `application.properties` content should be moved to the corresponding `.properties` file in the GitHub repo.

### Example: `billing-service.properties`
```properties
server.port=9005

# DB
spring.datasource.url=jdbc:mysql://localhost:3306/hms_billing_db
spring.datasource.username=root
spring.datasource.password=Ish983556

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Actuator
management.endpoints.web.exposure.include=health,info

# Kafka Consumer
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=billing-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
spring.kafka.consumer.properties.spring.deserializer.value.delegate.class=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*
spring.kafka.consumer.properties.spring.json.use.type.headers=false
spring.kafka.consumer.properties.spring.json.value.default.type=java.util.Map

# Redis Cache
spring.cache.type=redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.cache.redis.time-to-live=300000

# Circuit Breaker Configuration for Feign
spring.cloud.openfeign.circuitbreaker.enabled=true
resilience4j.circuitbreaker.instances.bookingService.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.bookingService.wait-duration-in-open-state=10s
resilience4j.circuitbreaker.instances.bookingService.sliding-window-size=10
resilience4j.circuitbreaker.instances.bookingService.minimum-number-of-calls=5
resilience4j.circuitbreaker.instances.bookingService.permitted-number-of-calls-in-half-open-state=3
resilience4j.circuitbreaker.instances.bookingService.automatic-transition-from-open-to-half-open-enabled=true

# Swagger/OpenAPI Configuration
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true
```

## Local Bootstrap Properties

Each microservice has a `bootstrap.properties` file that contains:
- Service name (for config server lookup)
- Config server URL
- Eureka configuration

These files remain in the local codebase and should NOT be committed to GitHub.

## Startup Order

1. **Eureka Server** (port 8761)
2. **Config Server** (port 8888)
3. **All Microservices** (they will fetch config from config server on startup)

## How It Works

1. Each microservice starts with `bootstrap.properties`
2. It connects to Config Server at `http://localhost:8888`
3. Config Server fetches the service-specific `.properties` file from GitHub
4. The microservice receives and applies the configuration
5. The microservice continues normal startup

## Updating Configuration

1. Update the `.properties` file in the GitHub repository
2. Commit and push changes
3. Restart the microservice OR call the `/actuator/refresh` endpoint (if enabled)

## Notes

- The `bootstrap.properties` files are kept local and contain only the config server connection details
- All environment-specific configurations (DB, Kafka, Redis, etc.) are in the GitHub repo
- The config server clones the GitHub repo on startup (`clone-on-start=true`)
- Default branch is `main`

