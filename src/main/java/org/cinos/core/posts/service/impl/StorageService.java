package org.cinos.core.posts.service.impl;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
            // Generar URL firmada que funciona sin configuración pública
            String signedUrl = generateSignedUrl(fileName);
            fileUrls.add(signedUrl);
        }
        return fileUrls;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Bucket bucket = storage.get(bucketName);
        Blob blob = bucket.create(fileName, file.getBytes());
        // Generar URL firmada que funciona sin configuración pública
        return generateSignedUrl(fileName);
    }

    public byte[] downloadFile(String fileName) throws IOException {
        Blob blob = storage.get(bucketName, fileName);
        return blob.getContent();
    }

    /**
     * Genera una URL firmada que funciona sin configuración pública del bucket
     * @param fileName nombre del archivo
     * @return URL firmada del archivo
     */
    private String generateSignedUrl(String fileName) {
        try {
            BlobId blobId = BlobId.of(bucketName, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
            
            // Generar URL firmada válida por 1 hora
            URL signedUrl = storage.signUrl(blobInfo, 1, TimeUnit.HOURS);
            return signedUrl.toString();
        } catch (Exception e) {
            // Fallback a URL pública si hay error
            return String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);
        }
    }
}
