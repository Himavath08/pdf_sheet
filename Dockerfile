FROM maven:3.9-eclipse-temurin-17 as builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-slim
WORKDIR /app
COPY --from=builder /app/target/metadata-extractor-1.0.0.jar .
EXPOSE 8080
CMD ["java", "-jar", "metadata-extractor-1.0.0.jar", "--http", "--port", "8080"]
