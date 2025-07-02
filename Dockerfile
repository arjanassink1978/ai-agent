# Build stage
FROM maven:3.9.6-eclipse-temurin-17-alpine as builder
WORKDIR /app

# Copy only pom.xml files first for dependency caching
COPY backend/pom.xml backend/pom.xml
COPY pom.xml pom.xml

# Download dependencies (cacheable layer)
RUN mvn -B -f backend/pom.xml dependency:go-offline

# Now copy the rest of the source
COPY . .

# Build the backend module
RUN mvn clean package -DskipTests -pl backend -am

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/backend/target/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]