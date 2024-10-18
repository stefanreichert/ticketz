# Use an official OpenJDK runtime as a parent image
FROM azul/zulu-openjdk-alpine:21

# Set the working directory in the container
WORKDIR /app

# Copy the application JAR file into the container
COPY target/ticketz-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]