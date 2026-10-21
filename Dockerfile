# Multi-stage Dockerfile pro aplikaci Dřevěnka s.r.o.
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Kopírování Gradle wrapperu a konfigurace
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle

# Ošetření Windows CRLF konců řádků v Alpine Linuxu a udělení práv pro spuštění
RUN chmod +x gradlew && sed -i 's/\r$//' gradlew

# Stažení závislostí
RUN ./gradlew dependencies --no-daemon || true

# Kopírování zdrojového kódu a sestavení spustitelného JARu
COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon

# Běhové prostředí (Runtime image)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Vytvoření neprivilegovaného uživatele pro bezpečný běh v kontejneru
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
