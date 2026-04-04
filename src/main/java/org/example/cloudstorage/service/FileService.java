package org.example.cloudstorage.service;

import io.minio.*;
import io.minio.messages.Item;
import org.example.cloudstorage.exception.GetFileException;
import org.example.cloudstorage.exception.UploadFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    public List<String> getFiles(String userPath) throws Exception {
        List<String> files = new ArrayList<>();

        try{
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(defaultBucket)
                            .prefix(userPath + "/")
                            .recursive(true)
                            .build()
            );

            for (Result<Item> result : results) {
                String fullName = result.get().objectName();

                if (fullName.length() > userPath.length() + 1) {
                    files.add(fullName.substring(userPath.length() + 1));
                }

            }
        }catch (Exception e){
            throw new GetFileException("Cannot get files");
        }

        return files;
    }

}
