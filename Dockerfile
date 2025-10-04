# Sử dụng một phiên bản Java 17 gọn nhẹ làm nền
FROM openjdk:17-jdk-slim

# Đặt thư mục làm việc bên trong container
WORKDIR /app

# Copy file pom.xml và các file mã nguồn vào container
COPY . .

# Cấp quyền thực thi cho file mvnw để sửa lỗi "Permission denied"
RUN chmod +x ./mvnw

# Chạy lệnh Maven để build project. Lệnh này sẽ tải các dependency và tạo ra file .jar
RUN ./mvnw clean install -DskipTests

# Cổng mà ứng dụng Spring Boot của bạn sẽ chạy (mặc định là 8080)
EXPOSE 8080

# Lệnh để chạy ứng dụng sau khi đã build xong
ENTRYPOINT ["java", "-jar", "target/ASM1-DUCDATH04243-SD20202-0.0.1-SNAPSHOT.jar"]