package org.cinos.core.posts.service.impl;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageService {

    @Value("${google.cloud.storage.bucket-name}")
    private String bucketName;
    private final Storage storage;

    public StorageService() {
        this.storage = StorageOptions.getDefaultInstance().getService();
    }

    public List<String> uploadFiles(List<MultipartFile> files) throws IOException {
        List<String> fileUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Bucket bucket = storage.get(bucketName);
            Blob blob = bucket.create(fileName, file.getBytes());
            fileUrls.add(blob.getMediaLink());  // URL pública del archivo
        }
        return fileUrls;
    }

    public String uploadFile(MultipartFile file) throws IOException {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Bucket bucket = storage.get(bucketName);
            Blob blob = bucket.create(fileName, file.getBytes());
        return blob.getMediaLink();
    }

    public byte[] downloadFile(String fileName) throws IOException {
        Blob blob = storage.get(bucketName, fileName);
        return blob.getContent();
    }

    /**
     * Descarga una imagen desde una URL del bucket
     * @param imageUrl URL de la imagen en el bucket
     * @return bytes de la imagen
     * @throws IOException si hay error al descargar
     */
    public byte[] downloadImageFromUrl(String imageUrl) throws IOException {
        try {
            // Extraer el nombre del archivo de la URL
            String fileName = extractFileNameFromUrl(imageUrl);
            return downloadFile(fileName);
        } catch (Exception e) {
            throw new IOException("Error al descargar la imagen desde la URL: " + imageUrl, e);
        }
    }

    /**
     * Extrae el nombre del archivo de una URL del bucket
     * @param imageUrl URL de la imagen
     * @return nombre del archivo
     */
    private String extractFileNameFromUrl(String imageUrl) {
        // La URL del bucket tiene formato: https://storage.googleapis.com/bucket-name/fileName
        // Necesitamos extraer solo el fileName
        if (imageUrl.contains(bucketName)) {
            // Buscar el nombre del archivo después del bucket name
            int bucketIndex = imageUrl.indexOf(bucketName);
            int fileNameStart = bucketIndex + bucketName.length() + 1; // +1 para el slash
            return imageUrl.substring(fileNameStart);
        } else {
            // Si no contiene el bucket name, intentar extraer el nombre del archivo de la URL
            String[] parts = imageUrl.split("/");
            return parts[parts.length - 1];
        }
    }
}
