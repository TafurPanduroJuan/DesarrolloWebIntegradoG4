# ── Etapa 1: Build ──────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copiar el descriptor de dependencias primero (cache de capas)
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

RUN chmod +x mvnw && \
    ./mvnw dependency:go-offline -q

# Copiar el resto del código y compilar
COPY src ./src
RUN ./mvnw clean package -DskipTests -q

# ── Etapa 2: Runtime ─────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar el JAR generado
COPY --from=build /app/target/*.jar app.jar

# Render inyecta la variable PORT (por defecto 8080)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]