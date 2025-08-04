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
            // Generar URL pública en lugar de URL que requiere autenticación
            String publicUrl = generatePublicUrl(fileName);
            fileUrls.add(publicUrl);
        }
        return fileUrls;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Bucket bucket = storage.get(bucketName);
        Blob blob = bucket.create(fileName, file.getBytes());
        // Generar URL pública en lugar de URL que requiere autenticación
        return generatePublicUrl(fileName);
    }

    public byte[] downloadFile(String fileName) throws IOException {
        Blob blob = storage.get(bucketName, fileName);
        return blob.getContent();
    }

    /**
     * Genera una URL pública para el archivo en Google Cloud Storage
     * @param fileName nombre del archivo
     * @return URL pública del archivo
     */
    private String generatePublicUrl(String fileName) {
        return String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);
    }
}
