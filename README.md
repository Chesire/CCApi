# CCApi

A backend service to handle the CCBot interactions

## Tech Stack

PostgreSQL database hosted on Neon  
Grafana is used for monitoring and visualization

## Deployment

### Production (Pre-built Image)

```yaml
version: '3.8'

services:
  ccapi:
    image: ghcr.io/chesire/ccapi:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=${DATABASE_URL}
      - GRAFANA_LOKI_URL=${GRAFANA_LOKI_URL}
      - GRAFANA_LOKI_USERNAME=${GRAFANA_LOKI_USERNAME}
      - GRAFANA_LOKI_PASSWORD=${GRAFANA_LOKI_PASSWORD}
      - GRAFANA_PROMETHEUS_URL=${GRAFANA_PROMETHEUS_URL}
      - GRAFANA_PROMETHEUS_USERNAME=${GRAFANA_PROMETHEUS_USERNAME}
      - GRAFANA_PROMETHEUS_PASSWORD=${GRAFANA_PROMETHEUS_PASSWORD}
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

**Environment Variables:**
```bash
# Neon PostgreSQL - Replace with your actual credentials
DATABASE_URL=postgresql://[username]:[password]@[host]/ccapi?sslmode=require

# Grafana Services - Replace with your actual credentials  
GRAFANA_LOKI_URL=https://logs-prod-us-central1.grafana.net
GRAFANA_LOKI_USERNAME=[your_username]
GRAFANA_LOKI_PASSWORD=[your_password]
GRAFANA_PROMETHEUS_URL=https://prometheus-prod-us-central1.grafana.net
GRAFANA_PROMETHEUS_USERNAME=[your_username]  
GRAFANA_PROMETHEUS_PASSWORD=[your_password]
```

⚠️ **Security Note:** Never commit actual credentials to version control. Use environment variables or secrets management.

**Version Pinning:**
- `ghcr.io/chesire/ccapi:latest` - Latest main branch
- `ghcr.io/chesire/ccapi:master-abc123def` - Specific commit SHA

## Development

### Local Build
```bash
git clone https://github.com/Chesire/CCApi.git
cd CCApi
docker-compose up --build
```

### PostgreSQL Testing
```bash
docker-compose --profile postgres up --build
```

## CI/CD

This project uses GitHub Actions for continuous integration and deployment:

- **Pull Requests:** Docker build validation and testing
- **Main Branch:** Automated image builds pushed to `ghcr.io/chesire/ccapi`

## TODO

Listed below are things that should probably be done at some point, or things that I think would be valuable to learn
about regardless:  
TODO: Add API documentation (Swagger or Postman)  
TODO: Add authentication and authorization (JWT or OAuth)  
TODO: Add rate limiting and throttling  
TODO: Add caching (Redis or Memcached)  
