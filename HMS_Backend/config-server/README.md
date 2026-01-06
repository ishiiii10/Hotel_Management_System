# Config Server

## What This Does

This is the configuration server. It centralizes configuration for all microservices. Instead of each service having its own config file, they can get config from here. (Note: Currently services are using their own config files, but this is set up for future use)

## How It Works

1. Config Server fetches configuration files from a Git repository
2. Config Server clones the Git repo on startup (or connects to it)
3. Microservices connect to Config Server on startup
4. Services fetch their configuration from Config Server (which gets it from Git)
5. If config changes in Git, services can refresh without restarting

## Configuration

### application.properties

```properties
spring.application.name=config-server
server.port=8888

# Config Server - GitHub Repository
spring.cloud.config.server.git.uri=https://github.com/ishiiii10/Hotel_Management_System_ConfigServer
spring.cloud.config.server.git.clone-on-start=true
spring.cloud.config.server.git.default-label=main
spring.cloud.config.server.git.skip-ssl-validation=false
```

The config server is configured to use a Git repository. It clones the repo on startup and serves config files from there.

## Config Files Structure

Config files are usually organized like:
```
config/
  auth-service.properties
  hotel-service.properties
  booking-service.properties
  ...
```

Or with profiles:
```
config/
  auth-service-dev.properties
  auth-service-prod.properties
  ...
```

## How Services Use It

Services need to be configured to use Config Server:

```properties
spring.cloud.config.uri=http://localhost:8888
spring.application.name=auth-service
spring.profiles.active=dev
```

Then the service will fetch config from:
```
http://localhost:8888/auth-service/dev
```

**Important:** The config server fetches the config files from the Git repository, and then services get them from the config server. So the flow is:
1. Config files are stored in Git repository
2. Config Server clones/reads from Git
3. Services request config from Config Server
4. Config Server serves the config from Git to the services

## Benefits

1. **Centralized Config** - All config in one place (Git repository)
2. **Environment-Specific** - Different configs for dev, test, prod
3. **Dynamic Refresh** - Can update config in Git, services refresh without restarting
4. **Version Control** - Config files in Git, can track changes, rollback if needed
5. **Security** - Can encrypt sensitive values
6. **Git-based** - All config is version controlled in Git, easy to manage and review

## Important Notes

- Currently services are using their own application.properties files
- Config Server is set up but not actively used yet
- Can be enabled later when needed
- Useful for managing config across multiple environments
- Can encrypt sensitive data like passwords

## Future Use

When ready to use:
1. Move all service configs to Config Server
2. Update services to fetch from Config Server
3. Use different profiles for different environments
4. Enable encryption for sensitive values

