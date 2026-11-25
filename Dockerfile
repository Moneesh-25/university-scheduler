FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY backend/pom.xml backend/pom.xml
COPY backend/mvnw backend/mvnw
COPY backend/.mvn backend/.mvn
COPY backend/src backend/src
RUN cd backend && chmod +x mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/backend/target/university-scheduler-1.0.0.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
