FROM openjdk:11.0.11-jdk-slim

WORKDIR /app

COPY target/spotify-api-integration-0.0.1-SNAPSHOT.jar /app/spotify-api-integration.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","spotify-api-integration.jar"]
