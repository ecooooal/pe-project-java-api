# Use Maven base image with OpenJDK 20
FROM maven:3.9.4-eclipse-temurin-20

# Set the working directory inside the container to /app
WORKDIR /app

# Copy everything from the current directory (on the host) to /app in the container
COPY . /app

RUN mvn dependency:go-offline

RUN mvn clean package -DskipTests

# Expose port 8082
EXPOSE 8082

CMD ["mvn", "compile", "exec:java"]
