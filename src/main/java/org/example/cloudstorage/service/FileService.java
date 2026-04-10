package org.example.cloudstorage.service;

import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import org.example.cloudstorage.exception.DeleteFileException;
import org.example.cloudstorage.exception.GetFileException;
import org.example.cloudstorage.exception.UploadFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;

import java.io.InputStream;
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

    public void uploadFile (String userPath, MultipartFile content) throws Exception {
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

    public void createFolder(String userPath) throws Exception {
        try{
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(defaultBucket)
                            .object(userPath + "/")
                            .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                            .build()
            );
        }catch (Exception e){
            throw new UploadFileException("Cannot upload folder");
        }
    }

    public InputStream getFile(String userPath) throws Exception {
        return minioClient.getObject(
           GetObjectArgs.builder()
                   .bucket(defaultBucket)
                   .object(userPath)
                   .build()
        );
    }

    public List<String> getFiles(String userPath) throws Exception {
        List<String> files = new ArrayList<>();

        try{
            Iterable<Result<Item>> results = listObjects(userPath, false);

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

    public void deleteFile(String userPath) throws Exception {
        try{
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(defaultBucket)
                            .object(userPath)
                            .build()
            );
        }catch (Exception e){
            throw new DeleteFileException("Cannot delete file");
        }
    }

    public void deleteFolder(String userPath) throws Exception {
        try{
            Iterable<Result<Item>> results = listObjects(userPath, true);
            List<DeleteObject> toDelete = new ArrayList<>();

            for (Result<Item> result : results) {
                toDelete.add(new DeleteObject(result.get().objectName()));
            }

            if(!toDelete.isEmpty()){
                Iterable<Result<DeleteError>> error = minioClient.removeObjects(
                        RemoveObjectsArgs.builder()
                                .bucket(defaultBucket)
                                .objects(toDelete)
                                .build()
                );

                for(Result<DeleteError> result : error){
                    result.get();
                }
            }
        }catch (Exception e){
            throw new DeleteFileException("Cannot delete folder");
        }
    }

    private Iterable<Result<Item>> listObjects(String fullPrefix, boolean recursive) throws Exception {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(defaultBucket)
                        .prefix(fullPrefix + "/")
                        .recursive(recursive)
                        .build()
        );
    }

}
