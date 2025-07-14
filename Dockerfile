# Usa Java 21 como base
FROM eclipse-temurin:21-jdk

# Directorio de trabajo
WORKDIR /app

# Copia el JAR (que ya debe contener resources como data.sql)
COPY target/*.jar app.jar

# Copia archivo de credenciales de GCP (NO subirlo al repo)
COPY authin-446020-4b361fe5ccb4.json /app/authin-446020-4b361fe5ccb4.json

# Variable para que Spring/Google lo detecte automáticamente
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/authin-446020-4b361fe5ccb4.json

# Exponer puerto (opcional)
EXPOSE 8080

# Ejecuta la app
CMD ["java", "-jar", "app.jar"]
