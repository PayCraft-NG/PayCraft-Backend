# Use a minimal JDK 17 base image
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the built Spring Boot application JAR file into the container
COPY target/paycraft-0.0.1-SNAPSHOT.jar /app/app.jar

# Expose the port the Spring Boot app runs on (default: 8080)
EXPOSE 6020

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
