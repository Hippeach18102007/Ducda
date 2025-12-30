# --- Giai đoạn 1: Build ứng dụng (Dùng image có sẵn Maven) ---
# Thêm "AS builder" để đặt tên cho giai đoạn này
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copy pom.xml và tải thư viện về trước (để tận dụng Cache của Docker)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy code nguồn và build
COPY src ./src
# Dùng "package" thay vì "install" cho nhẹ, skip test để build nhanh
RUN mvn clean package -DskipTests

# --- Giai đoạn 2: Run ứng dụng (Chỉ cần JRE để chạy cho nhẹ) ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy file .jar từ giai đoạn "builder" ở trên
# Lưu ý: Maven thường build ra file ở thư mục /target/
# Dấu * giúp tự bắt đúng tên file jar mà không cần gõ chính xác phiên bản
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 9000
ENTRYPOINT ["java", "-jar", "app.jar"]