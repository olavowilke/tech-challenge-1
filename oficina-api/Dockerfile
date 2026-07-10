# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

COPY settings.xml .

RUN chmod +x mvnw
RUN ./mvnw -s settings.xml dependency:go-offline -B

COPY src ./src
RUN ./mvnw -s settings.xml package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S oficina && adduser -S oficina -G oficina
USER oficina

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# Healthcheck via actuator (wget vem do busybox no alpine). O context-path é /api.
HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
    CMD wget -qO- http://localhost:8080/api/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
