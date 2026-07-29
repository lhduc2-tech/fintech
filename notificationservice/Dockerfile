# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom.xml va download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code va file chung chi Kafka
COPY src ./src
COPY ca.pem ./ca.pem

RUN mvn package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
RUN chown -R spring:spring /app

USER spring:spring

# Copy jar tu build stage
COPY --from=build /app/target/*.jar app.jar

# Copy ca.pem
COPY --from=build /app/ca.pem ./ca.pem

# Port mac dinh (dieu chinh neu khac)
EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
