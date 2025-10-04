# Sử dụng một phiên bản Java 17 gọn nhẹ làm nền
FROM openjdk:17-jdk-slim

# Đặt thư mục làm việc bên trong container
WORKDIR /app

# THÊM VÀO: Cập nhật và cài đặt các thư viện font cần thiết cho Apache POI
# Lần này cài cả fontconfig và libfreetype6
RUN apt-get update && apt-get install -y fontconfig libfreetype6

# Copy file pom.xml và các file mã nguồn vào container
COPY . .

# Cấp quyền thực thi cho file mvnw để sửa lỗi "Permission denied"
RUN chmod +x ./mvnw

# Chạy lệnh Maven để build project.
RUN ./mvnw clean install -DskipTests

# Cổng mà ứng dụng Spring Boot của bạn sẽ chạy
EXPOSE 8080

# Lệnh để chạy ứng dụng ở chế độ headless
ENTRYPOINT ["java", "-Djava.awt.headless=true", "-jar", "target/ASM1-DUCDATH04243-SD20202-0.0.1-SNAPSHOT.jar"]