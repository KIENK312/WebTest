# ========================================================
# Stage 1: Build mã nguồn Spring Boot bằng Maven
# ========================================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom.xml và thư mục source code src
COPY pom.xml .
COPY src ./src

# Build gói jar và bỏ qua test
RUN mvn clean package -DskipTests

# ========================================================
# Stage 2: Môi trường chạy JRE 17 siêu nhẹ (Alpine Linux)
# ========================================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy file jar đã build từ Stage 1
COPY --from=build /app/target/*.jar app.jar

# Cổng mặc định
EXPOSE 8080

# Chạy ứng dụng với profile h2 (In-memory database)
ENTRYPOINT ["java", "-Dspring.profiles.active=h2", "-jar", "app.jar"]
