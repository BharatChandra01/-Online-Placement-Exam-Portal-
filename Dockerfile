# --- STAGE 1: Build the application ---
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application and skip tests to save time
RUN mvn clean package -DskipTests

# --- STAGE 2: Run the application ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy only the built JAR file from the builder stage
# We use a wildcard (*) so it finds your jar regardless of the exact version name
COPY --from=build /app/target/*.jar app.jar

# Expose the port (Render will use this)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]