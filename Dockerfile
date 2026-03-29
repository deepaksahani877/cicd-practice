# Use the same major Java version as the Maven build target.
FROM eclipse-temurin:21-jdk-jammy

# Keep the runtime layout predictable inside the container.
WORKDIR /app

# The JAR is produced by `mvn clean verify` before Docker build runs.
COPY ./target/CICDPractice-0.0.1-SNAPSHOT.jar app.jar

# Start the Spring Boot application.
ENTRYPOINT ["java","-jar","app.jar"]
