## Use an official JDK image
#FROM eclipse-temurin:17-jdk
#
## Set working directory
#WORKDIR /app
#
## Copy the built JAR into the container
#COPY target/user-management-service-0.0.1-SNAPSHOT.jar app.jar
#
## Expose the port Render will assign
#EXPOSE 8080
#
## Run the JAR
#ENTRYPOINT ["java","-jar","app.jar"]

## Stage 1: Build the JAR
#FROM eclipse-temurin:17-jdk AS build
#WORKDIR /app
#COPY . .
#RUN ./mvnw clean package -DskipTests
#
## Stage 2: Run the JAR
#FROM eclipse-temurin:17-jdk
#WORKDIR /app
#COPY --from=build /app/target/user-management-service-0.0.1-SNAPSHOT.jar app.jar
#EXPOSE 8080
#ENTRYPOINT ["java","-jar","app.jar"]



FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
