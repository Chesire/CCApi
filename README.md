# CCApi

A backend service to handle the CCBot interactions

## Tech Stack

PostgreSQL database hosted on Neon  
Grafana is used for monitoring and visualization

## Environment Variables

### Required for Production

The following environment variables **must** be provided when running in production mode (`SPRING_PROFILES_ACTIVE=prod`):

| Variable | Description | Example |
|----------|-------------|---------|
| `NEON_URL` | PostgreSQL database connection URL | `jdbc:postgresql://your-db.neon.tech/ccapi?sslmode=require` |
| `NEON_USERNAME` | PostgreSQL database username | `your_username` |
| `NEON_PASSWORD` | PostgreSQL database password | `your_secure_password` |
| `JWT_SECRET` | Secret key for JWT token signing (base64 encoded, min 256 bits) | `3jFxCSLcbQLEgec2h8ZvWYMltaCOw4Nsobftl2GP0lQ=` |
| `CAPI_API_KEY` | API key for authentication endpoint (min 30 chars) | `your-secure-api-key-min-30-characters` |
| `GRAFANA_LOKI_URL` | Grafana Loki endpoint for logging | `https://logs-prod-us-central1.grafana.net` |
| `GRAFANA_LOKI_USERNAME` | Grafana Loki username | `123456` |
| `GRAFANA_LOKI_PASSWORD` | Grafana Loki password | `your_loki_password` |
| `GRAFANA_PROMETHEUS_URL` | Grafana Prometheus endpoint for metrics | `https://prometheus-prod-us-central1.grafana.net` |
| `GRAFANA_PROMETHEUS_USERNAME` | Grafana Prometheus username | `654321` |
| `GRAFANA_PROMETHEUS_PASSWORD` | Grafana Prometheus password | `your_prometheus_password` |

**Optional Production Variables:**

| Variable | Description | Default |
|----------|-------------|---------|
| `JWT_EXPIRATION` | JWT token expiration time in milliseconds | `3600000` (1 hour) |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` |

### Development Environment

For local development, most variables have sensible defaults and can be overridden if needed:

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `JWT_SECRET` | Secret key for JWT signing | `3jFxCSLcbQLEgec2h8ZvWYMltaCOw4Nsobftl2GP0lQ=` |
| `CAPI_API_KEY` | API key for authentication | `dev-default-api-key-extended` |

The development profile uses an in-memory H2 database, so no database credentials are needed.

Grafana integration is disabled by default in development mode.

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
      - NEON_URL=${NEON_URL}
      - NEON_USERNAME=${NEON_USERNAME}
      - NEON_PASSWORD=${NEON_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
      - CAPI_API_KEY=${CAPI_API_KEY}
      - GRAFANA_LOKI_URL=${GRAFANA_LOKI_URL}
      - GRAFANA_LOKI_USERNAME=${GRAFANA_LOKI_USERNAME}
      - GRAFANA_LOKI_PASSWORD=${GRAFANA_LOKI_PASSWORD}
      - GRAFANA_PROMETHEUS_URL=${GRAFANA_PROMETHEUS_URL}
      - GRAFANA_PROMETHEUS_USERNAME=${GRAFANA_PROMETHEUS_USERNAME}
      - GRAFANA_PROMETHEUS_PASSWORD=${GRAFANA_PROMETHEUS_PASSWORD}
      - JWT_EXPIRATION=3600000  # Optional: 1 hour
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

**Example Production .env File:**
```bash
# Database Configuration (Neon PostgreSQL)
NEON_URL=jdbc:postgresql://ep-cool-darkness-123456.us-east-2.aws.neon.tech/ccapi?sslmode=require
NEON_USERNAME=ccapi_user
NEON_PASSWORD=super_secure_password_123

# Authentication & Security
JWT_SECRET=3jFxCSLcbQLEgec2h8ZvWYMltaCOw4Nsobftl2GP0lQ=
CAPI_API_KEY=production-api-key-must-be-at-least-30-characters-long
JWT_EXPIRATION=3600000

# Grafana Loki (Logging)
GRAFANA_LOKI_URL=https://logs-prod-us-central1.grafana.net
GRAFANA_LOKI_USERNAME=123456
GRAFANA_LOKI_PASSWORD=glc_your_loki_api_key_here

# Grafana Prometheus (Metrics)
GRAFANA_PROMETHEUS_URL=https://prometheus-prod-us-central1.grafana.net
GRAFANA_PROMETHEUS_USERNAME=654321
GRAFANA_PROMETHEUS_PASSWORD=your_prometheus_api_key_here
```

⚠️ **Security Note:** Never commit actual credentials to version control. Use environment variables or secrets management.

**Version Pinning:**
- `ghcr.io/chesire/ccapi:latest` - Latest main branch
- `ghcr.io/chesire/ccapi:master-abc123def` - Specific commit SHA

## Development

### Local Build

For local development, the application uses H2 in-memory database with default credentials:

```bash
git clone https://github.com/Chesire/CCApi.git
cd CCApi
docker-compose up --build
```

**Example Development .env File (Optional):**
```bash
# Optional: Override default development values if needed
JWT_SECRET=dev-jwt-secret-key-for-local-testing-only
CAPI_API_KEY=dev-default-api-key-extended
```

The dev profile automatically configures:
- H2 in-memory database (no setup required)
- H2 console enabled at `/h2-console`
- Grafana integration disabled
- Debug logging enabled
- All actuator endpoints exposed

### PostgreSQL Testing

To test with PostgreSQL locally instead of H2:

```bash
docker-compose --profile postgres up --build
```

This starts a PostgreSQL container alongside the application. Override database settings:
```bash
SPRING_PROFILES_ACTIVE=prod
NEON_URL=jdbc:postgresql://postgres:5432/ccapi
NEON_USERNAME=ccapi
NEON_PASSWORD=change_me_in_production
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
