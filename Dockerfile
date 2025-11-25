FROM gradle:8.5-jdk22-alpine AS build

WORKDIR /app

# Copy Gradle files first for dependency caching
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Download dependencies
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Build the application
RUN gradle build --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:22-jre-alpine

WORKDIR /app

# Install wget for healthcheck
RUN apk add --no-cache wget

# Copy the built JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8082

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

