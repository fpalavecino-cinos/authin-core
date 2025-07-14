# Imagen base con Java 17
FROM eclipse-temurin:21-jdk

# Directorio de trabajo
WORKDIR /app

# Copiar el archivo JAR (ya compilado)
COPY target/*.jar app.jar

# Copiar el archivo de credenciales (¡NO subirlo al repo!)
COPY authin-446020-4b361fe5ccb4.json /app/authin-446020-4b361fe5ccb4.json

# Variable de entorno para que tu app lo use
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/authin-446020-4b361fe5ccb4.json

# Exponer el puerto (opcional)
EXPOSE 8080

# Comando para correr la app
CMD ["java", "-jar", "app.jar"]
