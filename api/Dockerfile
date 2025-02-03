# API Dockerfile
FROM openjdk:17-jdk-slim

# Install Maven
RUN apt-get update && apt-get install -y maven

# Set the working directory to /app
WORKDIR /app

# Copy the parent POM
COPY ../pom.xml ./pom.xml

# Copy the settings file
COPY ../settings.xml /root/.m2/settings.xml

# Copy the API module
COPY ./api /app/api

# Change directory to the API module
WORKDIR /app/api

# Package the application
RUN mvn clean package -Dmaven.wagon.http.retryHandler.count=3

# Run the application
CMD ["java", "-jar", "target/api-1.0-SNAPSHOT.jar"]
