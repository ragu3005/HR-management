# Build Stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and dependencies
COPY backend/pom.xml ./backend/
RUN mvn -f backend/pom.xml dependency:go-offline -B

# Copy backend source code (including static frontend files)
COPY backend/src ./backend/src

# Package application
RUN mvn -f backend/pom.xml clean package -DskipTests

# Runtime Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create directory for uploads
RUN mkdir -p /app/uploads

# Copy built jar
COPY --from=build /app/backend/target/*.jar app.jar

# Expose port (Render sets $PORT dynamically)
ENV PORT=8080
EXPOSE 8080

# Run Spring Boot with optimized memory for Render 512MB Free Tier
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -Xmx350m -Xms128m -Xss512k -Dserver.port=${PORT} -jar app.jar"]

