# Docker Setup Guide

## Overview

This docker-compose.yml file includes all services needed to run the Hotel Management System backend, including infrastructure services (MySQL, Redis, Kafka, Zookeeper) and all microservices.

## Architecture

All services run in Docker containers and communicate through a dedicated Docker network.

## Prerequisites

- **Docker** and **Docker Compose** installed
- **Git** (for Config Server to fetch configuration from GitHub)
- At least **8GB RAM** available for Docker
- **Ports available**: 3307-3311, 6379, 8761, 8888, 9001-9006, 9090, 9092, 2181

## Services Included

### Infrastructure Services

1. **MySQL Databases** (5 instances)
   - `mysql-auth` (port 3307) - Auth service database
   - `mysql-hotel` (port 3308) - Hotel service database
   - `mysql-booking` (port 3309) - Booking service database
   - `mysql-billing` (port 3310) - Billing service database
   - `mysql-notification` (port 3311) - Notification service database

2. **Redis** (port 6379)
   - Used by Reports Service for caching

3. **Zookeeper** (port 2181)
   - Required by Kafka for coordination

4. **Kafka** (port 9092)
   - Event streaming platform
   - Used for asynchronous communication between services

### Microservices

1. **Eureka Service** (port 8761)
   - Service discovery and registration
   - Access dashboard at: http://localhost:8761

2. **Config Server** (port 8888)
   - Centralized configuration management
   - Fetches configuration from GitHub repository

3. **Auth Service** (port 9001)
   - User authentication and authorization
   - JWT token management

4. **Hotel Service** (port 9002)
   - Hotel and room management

5. **Booking Service** (port 9003)
   - Booking creation and management
   - Publishes events to Kafka

6. **Notification Service** (port 9004)
   - Email and SMS notifications
   - Consumes events from Kafka

7. **Billing Service** (port 9005)
   - Bill generation and payment processing
   - Consumes events from Kafka

8. **Reports Service** (port 9006)
   - Analytics and reporting
   - Uses Redis for caching

9. **API Gateway** (port 9090)
   - Single entry point for all API requests
   - Route forwarding and authentication

## Usage

### Start All Services

```bash
cd HMS_Backend
docker-compose up -d
```

This will:
- Build Docker images for all microservices
- Start all infrastructure services (MySQL, Redis, Kafka, Zookeeper)
- Start all microservices in the correct order based on dependencies
- Create necessary Docker networks and volumes

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f auth-service
docker-compose logs -f kafka
docker-compose logs -f booking-service
```

### Stop All Services

```bash
docker-compose down
```

To also remove volumes (⚠️ **WARNING**: This will delete all data):
```bash
docker-compose down -v
```

### Rebuild After Code Changes

```bash
docker-compose up -d --build
```

### Check Service Status

```bash
docker-compose ps
```

### Access Service Dashboards

- **Eureka Dashboard**: http://localhost:8761
- **Config Server**: http://localhost:8888/actuator/health
- **API Gateway**: http://localhost:9090

## Service Dependencies

Services start in the following order:

1. **Infrastructure Layer**:
   - MySQL databases
   - Zookeeper
   - Kafka (depends on Zookeeper)
   - Redis

2. **Service Discovery Layer**:
   - Eureka Service
   - Config Server (depends on Eureka)

3. **Business Services**:
   - Auth Service
   - Hotel Service
   - Booking Service (depends on Kafka)
   - Notification Service (depends on Kafka)
   - Billing Service (depends on Kafka)
   - Reports Service (depends on Redis)

4. **Gateway Layer**:
   - API Gateway (depends on all services)

## Database Setup

The MySQL containers will automatically create the databases on first startup. However, you may need to run migrations or initialize data depending on your application setup.

To access a database directly:
```bash
# Connect to auth database
docker exec -it hms-mysql-auth mysql -uroot -pIsh983556 hms_auth_db

# Connect to booking database
docker exec -it hms-mysql-booking mysql -uroot -pIsh983556 hms_booking_db
```

## Kafka Topics

Kafka will automatically create topics when needed. Common topics used:
- `booking-created`
- `booking-confirmed`
- `booking-checked-in`
- `booking-completed`

To list topics:
```bash
docker exec -it hms-kafka kafka-topics --list --bootstrap-server localhost:9092
```

## Troubleshooting

### Services Won't Start

1. **Check if ports are already in use:**
   ```bash
   # Check specific port
   lsof -i :9090
   
   # Check all used ports
   docker-compose ps
   ```

2. **Check Docker resources:**
   ```bash
   docker stats
   ```
   Ensure you have enough memory allocated to Docker.

3. **View service logs:**
   ```bash
   docker-compose logs [service-name]
   ```

### Database Connection Issues

1. **Wait for databases to be ready:**
   ```bash
   docker-compose ps
   ```
   Ensure all MySQL services show as "healthy"

2. **Check database logs:**
   ```bash
   docker-compose logs mysql-auth
   ```

### Kafka Connection Issues

1. **Verify Kafka is running:**
   ```bash
   docker-compose ps kafka
   ```

2. **Check Kafka logs:**
   ```bash
   docker-compose logs kafka
   ```

3. **Test Kafka connectivity:**
   ```bash
   docker exec -it hms-kafka kafka-broker-api-versions --bootstrap-server localhost:9092
   ```

### Service Health Checks Failing

Health checks use `curl` which might not be available in the minimal JRE image. Services will still work, but health checks may fail. You can:
- Remove healthcheck sections if not needed
- Use a base image with curl installed
- Use TCP health checks instead

### Out of Memory Errors

If you encounter memory issues:
1. Increase Docker Desktop memory allocation (Settings → Resources → Memory)
2. Reduce the number of services running simultaneously
3. Use `docker-compose up` without `-d` to see real-time logs and identify problematic services

## Port Summary

| Service | Port | Description |
|---------|------|-------------|
| Eureka | 8761 | Service Discovery Dashboard |
| Config Server | 8888 | Configuration Management |
| Auth Service | 9001 | Authentication |
| Hotel Service | 9002 | Hotel Management |
| Booking Service | 9003 | Booking Management |
| Notification Service | 9004 | Notifications |
| Billing Service | 9005 | Billing |
| Reports Service | 9006 | Reports & Analytics |
| API Gateway | 9090 | API Gateway |
| Kafka | 9092 | Event Streaming |
| Zookeeper | 2181 | Kafka Coordination |
| Redis | 6379 | Caching |
| MySQL Auth | 3307 | Auth Database |
| MySQL Hotel | 3308 | Hotel Database |
| MySQL Booking | 3309 | Booking Database |
| MySQL Billing | 3310 | Billing Database |
| MySQL Notification | 3311 | Notification Database |

## Data Persistence

All data is persisted in Docker volumes:
- `mysql_auth_data` - Auth database data
- `mysql_hotel_data` - Hotel database data
- `mysql_booking_data` - Booking database data
- `mysql_billing_data` - Billing database data
- `mysql_notification_data` - Notification database data
- `redis_data` - Redis cache data

To backup data:
```bash
docker run --rm -v hms_mysql_auth_data:/data -v $(pwd):/backup alpine tar czf /backup/mysql_auth_backup.tar.gz /data
```

## Development Tips

1. **View all logs in real-time:**
   ```bash
   docker-compose logs -f
   ```

2. **Restart a specific service:**
   ```bash
   docker-compose restart auth-service
   ```

3. **Scale a service (if needed):**
   ```bash
   docker-compose up -d --scale booking-service=2
   ```

4. **Execute commands in a running container:**
   ```bash
   docker exec -it hms-auth-service sh
   ```

5. **Clean up everything:**
   ```bash
   docker-compose down -v
   docker system prune -a
   ```

## Production Considerations

For production deployment, consider:
- Using external managed databases (RDS, Cloud SQL, etc.)
- Using managed Kafka (Confluent Cloud, AWS MSK, etc.)
- Using managed Redis (ElastiCache, Redis Cloud, etc.)
- Setting up proper monitoring and logging
- Configuring resource limits
- Using secrets management for sensitive data
- Setting up backup strategies
- Configuring SSL/TLS for all connections
