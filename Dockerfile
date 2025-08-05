FROM maven:3.9.11-eclipse-temurin-21-alpine

# Setup working directory
WORKDIR /app

# Copy full project
COPY . /app

# Install dependencies and package fat jar (skip tests)
RUN mvn clean package -DskipTests

# Expose server port
EXPOSE 8090

# Run the app with java (not mvn exec, to avoid re-compilation)
CMD ["java", "-jar", "target/java-api.jar"]
