# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code và file chứng chỉ vào để build
COPY src ./src
# Đảm bảo file ca.pem ở thư mục gốc của paymentgateway được đưa vào docker build
COPY ca.pem ./ca.pem

RUN mvn package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Biện pháp bảo mật: Cấp quyền cho user 'spring' có thể đọc được file cert
RUN chown -R spring:spring /app

USER spring:spring

# 1. Copy file jar từ build stage sang
COPY --from=build /app/target/*.jar app.jar

# 2. CHÍNH LÀ ĐÂY: Copy file ca.pem từ build stage sang cùng cấp với file app.jar
COPY --from=build /app/ca.pem ./ca.pem

# Expose port (default Spring Boot port)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]