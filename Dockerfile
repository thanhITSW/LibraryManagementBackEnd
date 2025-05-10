# Stage 1: Build ứng dụng
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy file cấu hình Maven và tải dependencies trước
COPY pom.xml ./
RUN mvn dependency:go-offline

# Copy toàn bộ mã nguồn và build ứng dụng
COPY src/ ./src/
RUN mvn clean package -DskipTests

# Stage 2: Tạo image tối giản chỉ với OpenJDK và file .jar
FROM openjdk:21-jdk
WORKDIR /app

# Copy file jar từ stage build
COPY --from=build /app/target/*.jar app.jar

# Mở cổng 9000
EXPOSE 9000

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
