# Stage 1: Build the project
FROM maven:3.8.3-openjdk-11 AS build
WORKDIR /app
COPY spotify-api-integration/pom.xml .
COPY spotify-api-integration/src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the app
FROM openjdk:11.0.11-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

