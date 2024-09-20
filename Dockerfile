FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/*.jar /app/app.jar

EXPOSE 6020

ENTRYPOINT ["java", "-jar", "app.jar"]
