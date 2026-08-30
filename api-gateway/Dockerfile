FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package

FROM eclipse-temurin:21-jdk AS runner

WORKDIR /app

ENV AUTH_SERVICE_URL=http://auth-service:4005
ENV PATIENT_SERVICE_URL=http://patient-service:4000

COPY --from=builder /app/target/*.jar app.jar
EXPOSE 4004

ENTRYPOINT ["java", "-jar", "app.jar"]