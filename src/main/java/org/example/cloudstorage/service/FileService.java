package org.example.cloudstorage.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.example.cloudstorage.exception.UploadFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {
    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String defaultBucket;

    public FileService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public void upload(String userPath, MultipartFile content) throws Exception {
        try{
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(defaultBucket)
                            .object(userPath + "/" + content.getOriginalFilename())
                            .stream(content.getInputStream(), content.getSize(), -1)
                            .contentType(content.getContentType())
                            .build()
            );
        }catch (Exception e){
            throw new UploadFileException("Cannot upload file");
        }
    }

}
