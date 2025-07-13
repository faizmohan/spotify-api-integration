FROM maven:3.8.3-openjdk-11 AS build
WORKDIR spotify-api-integration
COPY . spotify-api-integration
RUN mvn -f spotify-api-integration/pom.xml clean package -DskipTests

FROM openjdk:11.0.11-jdk-slim
WORKDIR spotify-api-integration
COPY --from=build spotify-api-integration/target/spotify-api-integration-0.0.1-SNAPSHOT.jar spotify-api-integration.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","spotify-api-integration.jar"]
