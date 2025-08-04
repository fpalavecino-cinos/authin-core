package org.cinos.core.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Cors;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Configuration
public class GoogleCloudStorageConfig {

    @Value("${google.cloud.storage.bucket-name}")
    private String bucketName;

    @Value("${GCP_CREDENTIALS_BASE64}")
    private String gcpCredentialsBase64;

    private Storage storage;

    @Bean
    public Storage storage() throws IOException {
        // Decodificar y escribir a un archivo temporal
        byte[] decoded = Base64.getDecoder().decode(gcpCredentialsBase64);
        Path tempFile = Files.createTempFile("gcp", ".json");
        Files.write(tempFile, decoded);

        // Construir el Storage client con el archivo temporal
        GoogleCredentials credentials = GoogleCredentials.fromStream(Files.newInputStream(tempFile));
        this.storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        
        return this.storage;
    }

    @PostConstruct
    public void configureBucket() {
        try {
            Bucket bucket = storage.get(bucketName);
            if (bucket != null) {
                // Configurar CORS para permitir acceso desde cualquier origen
                Cors cors = Cors.newBuilder()
                        .setOrigins(Arrays.asList("*"))
                        .setMethods(Arrays.asList(HttpMethod.GET, HttpMethod.HEAD))
                        .setResponseHeaders(Arrays.asList("Content-Type", "Access-Control-Allow-Origin"))
                        .setMaxAgeSeconds(3600)
                        .build();

                bucket.toBuilder()
                        .setCors(Arrays.asList(cors))
                        .build()
                        .update();

                System.out.println("Bucket CORS configurado correctamente");
            }
        } catch (Exception e) {
            System.err.println("Error al configurar CORS del bucket: " + e.getMessage());
        }
    }
}

