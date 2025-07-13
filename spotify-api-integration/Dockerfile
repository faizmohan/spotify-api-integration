FROM maven:3.8.3-openjdk-11

WORKDIR /app

COPY target/spotify-api-integration-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]




