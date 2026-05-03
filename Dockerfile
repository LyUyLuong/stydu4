# Multi-stage build for Spring Boot application

# Stage 1: Build stage
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true -B

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Create non-root user for security, plus writable directories owned by it.
# (mkdir/chown must run as root, BEFORE the USER switch.)
RUN addgroup -S spring && adduser -S spring -G spring \
 && mkdir -p /app/storage /app/logs \
 && chown -R spring:spring /app

# Copy JAR from build stage (chown so the runtime user can read it)
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

USER spring:spring

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/actuator/health || exit 1

# Environment variables (can be overridden by docker-compose)
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
