# Use lightweight Java runtime
FROM eclipse-temurin:17-jdk-alpine

# Create work directory
WORKDIR /app

# Copy jar file
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Expose Spring Boot port
EXPOSE 8080

# Pass all environment variables automatically
ENTRYPOINT ["java","-jar","/app/app.jar"]
