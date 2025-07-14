# Etapa 1: Build del JAR con Maven y Java 21
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final de runtime
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copiar JAR desde el build anterior
COPY --from=build /app/target/*.jar app.jar

# Copiar el script que genera el archivo de credenciales
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

# Variables de entorno
ENV GOOGLE_CREDENTIALS_FILE=/app/credentials.json

# Exponer el puerto que usa Spring Boot
EXPOSE 8080

# Comando que corre el script de arranque (que genera el .json y ejecuta la app)
ENTRYPOINT ["/app/entrypoint.sh"]
