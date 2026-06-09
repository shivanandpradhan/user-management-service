# Use an official JDK image
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy the built JAR into the container
COPY target/user-management-service-0.0.1-SNAPSHOT.jar app.jar

# Expose the port Render will assign
EXPOSE 8080

# Run the JAR
ENTRYPOINT ["java","-jar","app.jar"]
