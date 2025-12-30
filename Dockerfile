FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean install -DskipTests

# --- Giai đoạn 2: Run ứng dụng ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# SỬA DÒNG NÀY: Tên file .jar phải là "ny-0.0.1-SNAPSHOT.jar"
COPY --from=builder /app/target/ny-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 9000
ENTRYPOINT ["java", "-jar", "app.jar"]