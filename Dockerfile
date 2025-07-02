# Build stage: use Maven image with JDK 17
FROM maven:3.9.6-eclipse-temurin-17-alpine as builder
WORKDIR /app
COPY backend/. .
RUN mvn clean package -DskipTests

# Runtime stage: use JRE 17
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]