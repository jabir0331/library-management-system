# syntax=docker/dockerfile:1

# ---- Build stage: compile and package the jar -------------------------------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Copy only what Maven needs to resolve dependencies first, so this layer is
# cached until pom.xml changes
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B -q package -DskipTests

# ---- Runtime stage: small JRE image, non-root user --------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app --no-create-home app
COPY --from=build /workspace/target/*.jar app.jar
USER app

EXPOSE 8080

# Configuration (DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET, ...) comes from environment variables
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
