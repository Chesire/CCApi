# Multi-stage Docker build for Spring Boot application
# Stage 1: Build the application - Use Eclipse Temurin JDK 23 + Gradle Wrapper
FROM eclipse-temurin:23-jdk-alpine AS builder

WORKDIR /app

# Copy gradle wrapper files (most efficient approach)
COPY gradlew ./
COPY gradle/ ./gradle/

# Copy gradle files first for better caching
COPY build.gradle.kts settings.gradle.kts ./

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon

# Copy source code and build
COPY src/ ./src/
RUN ./gradlew bootJar --no-daemon

# Stage 2: Runtime image - Use Eclipse Temurin (matches GitHub Actions)
FROM eclipse-temurin:23-jre-alpine

WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Create non-root user for security
RUN addgroup -S appuser && adduser -S appuser -G appuser

# Copy the JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership to non-root user
RUN chown appuser:appuser app.jar

# Switch to non-root user
USER appuser

# Expose the port Spring Boot runs on
EXPOSE 8080

# Health check for container orchestration
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application with optimized JVM settings
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]