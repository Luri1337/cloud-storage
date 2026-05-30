package org.example.cloudstorage.service;

import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import org.example.cloudstorage.dto.StorageObjectDto;
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

    public void createFolder(String userPath) {
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

    public List<StorageObjectDto> getFiles(String userPath) {
        List<StorageObjectDto> files = new ArrayList<>();

        try{
            Iterable<Result<Item>> results = listObjects(userPath, false);

            for (Result<Item> result : results) {
                Item item = result.get();
                String fullName = item.objectName();
                String relativeName = fullName.substring(userPath.length() + 1);

                boolean isDir = relativeName.endsWith("/");
                String type = isDir ? "DIRECTORY" : "FILE";
                String name = isDir ? relativeName : relativeName.substring(relativeName.lastIndexOf("/") + 1);
                String path = isDir ? "" : relativeName.substring(0, relativeName.lastIndexOf("/") + 1);
                long size = item.size();

                files.add(new StorageObjectDto(name, path, size, type));
            }
        }catch (Exception e){
            throw new GetFileException("Cannot get files");
        }

        return files;
    }

    public void deleteFile(String userPath) {
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

    public void deleteFolder(String userPath) {
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

    private Iterable<Result<Item>> listObjects(String fullPrefix, boolean recursive) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(defaultBucket)
                        .prefix(fullPrefix + "/")
                        .recursive(recursive)
                        .build()
        );
    }

}
