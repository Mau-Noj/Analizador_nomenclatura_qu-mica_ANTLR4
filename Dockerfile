# ── Etapa 1: Build ────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copiar archivos de Maven primero (cachea dependencias si no cambian)
COPY pom.xml .
COPY src ./src

# Compilar y empaquetar (sin tests para acelerar el build)
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests

# ── Etapa 2: Runtime ──────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar solo el JAR generado
COPY --from=build /app/target/quimica-analizador-1.0.0.jar app.jar

# Puerto que expone Spring Boot
EXPOSE 8080

# Variables de entorno opcionales
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Arrancar la aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]