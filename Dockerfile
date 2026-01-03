# --- STAGE 1: Build using Maven and Java 21 ---
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom and source
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# --- STAGE 2: Run using Java 21 Runtime ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built jar file
COPY --from=build /app/target/*.jar app.jar

# Render uses port 8080 by default for Spring Boot
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]