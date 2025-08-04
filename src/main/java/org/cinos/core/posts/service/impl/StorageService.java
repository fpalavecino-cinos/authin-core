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
            System.out.println("URL original: " + imageUrl);
            System.out.println("Nombre del archivo extraído: " + fileName);
            System.out.println("Bucket name: " + bucketName);
            
            // Verificar que el blob existe antes de descargarlo
            Blob blob = storage.get(bucketName, fileName);
            if (blob == null) {
                throw new IOException("El archivo no existe en el bucket: " + fileName);
            }
            
            return blob.getContent();
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
        try {
            System.out.println("Extrayendo nombre de archivo de: " + imageUrl);
            
            // La URL puede ser de diferentes formatos:
            // 1. https://storage.googleapis.com/download/storage/v1/b/bucket-name/o/fileName?generation=...&alt=media
            // 2. https://storage.googleapis.com/bucket-name/fileName
            // 3. https://bucket-name.storage.googleapis.com/fileName
            
            // Primero, eliminar parámetros de query si existen
            String urlWithoutParams = imageUrl;
            if (imageUrl.contains("?")) {
                urlWithoutParams = imageUrl.substring(0, imageUrl.indexOf("?"));
                System.out.println("URL sin parámetros: " + urlWithoutParams);
            }
            
            // Buscar el nombre del archivo después de "/o/" (objeto)
            if (urlWithoutParams.contains("/o/")) {
                int objectIndex = urlWithoutParams.indexOf("/o/");
                String fileName = urlWithoutParams.substring(objectIndex + 3); // +3 para "/o/"
                System.out.println("Nombre extraído después de /o/: " + fileName);
                return fileName;
            }
            
            // Si no contiene "/o/", buscar después del bucket name
            if (urlWithoutParams.contains(bucketName)) {
                int bucketIndex = urlWithoutParams.indexOf(bucketName);
                int fileNameStart = bucketIndex + bucketName.length() + 1; // +1 para el slash
                String fileName = urlWithoutParams.substring(fileNameStart);
                System.out.println("Nombre extraído después del bucket: " + fileName);
                return fileName;
            }
            
            // Fallback: extraer el último segmento de la URL
            String[] parts = urlWithoutParams.split("/");
            String fileName = parts[parts.length - 1];
            System.out.println("Nombre extraído del último segmento: " + fileName);
            return fileName;
            
        } catch (Exception e) {
            System.err.println("Error al extraer nombre de archivo: " + e.getMessage());
            throw new IllegalArgumentException("No se pudo extraer el nombre del archivo de la URL: " + imageUrl, e);
        }
    }
}
