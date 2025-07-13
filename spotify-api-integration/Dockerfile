# ---------- Build stage ----------
FROM maven:3.8.3-openjdk-11 AS build
WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# ---------- Runtime stage ----------
FROM openjdk:11.0.11-jdk-slim
WORKDIR /app
COPY --from=build /app/target/spotify-api-integration-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
