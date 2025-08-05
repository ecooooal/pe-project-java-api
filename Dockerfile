# Stage 1: Build
FROM maven:3.9.11-eclipse-temurin-21-alpine AS builder

WORKDIR /app
COPY . /app

# Optional: preload dependencies
RUN mvn dependency:go-offline

# Compile without tests
RUN mvn compile

# Stage 2: Runtime
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy project and compiled code
COPY --from=builder /app /app

# Expose port used by your HTTP server
EXPOSE 8082

# Launch using exec-maven-plugin
CMD ["mvn", "compile", "exec:java"]

# -----------------------------------------------
# OPTIONAL RESOURCE LIMITS (use in docker-compose)
# -----------------------------------------------
# In docker-compose.yml:
#
# deploy:
#   resources:
#     limits:
#       memory: 512M
#       cpus: '0.5'
#
# Note: 'deploy' only works in swarm mode. For dev/testing, use:
#    mem_limit: 512m
#    cpus: 0.5
