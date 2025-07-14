# Imagen base con Java 21
FROM eclipse-temurin:21-jdk

# Directorio de trabajo
WORKDIR /app

# Copiar el JAR compilado
COPY target/*.jar app.jar

# Copiar el script de arranque (opcional si usás ENTRYPOINT)
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

# Exponer puerto (opcional para documentación)
EXPOSE 8080

# Variables de entorno
ENV GOOGLE_CREDENTIALS_FILE=/app/authin-446020-4b361fe5ccb4.json

# ENTRYPOINT que crea el archivo de credenciales desde la variable base64
ENTRYPOINT ["/app/entrypoint.sh"]
