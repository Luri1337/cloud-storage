package org.example.cloudstorage.controller;

import jakarta.servlet.ServletRequest;
import org.example.cloudstorage.entity.User;
import org.example.cloudstorage.service.FileService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "path", defaultValue = "") String path,
            @AuthenticationPrincipal User user
    ) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Empty file");
        }

        try {
            fileService.uploadFile(buildFullPath(user.getId(), path), file);
            return ResponseEntity.ok("Uploaded file successfully");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/createFolder")
    public ResponseEntity<String> createFolder(
            @AuthenticationPrincipal User user,
            @RequestParam(value = "path", defaultValue = "") String path

    ) {
        fileService.createFolder(buildFullPath(user.getId(), path));
        return ResponseEntity.ok("Folder created successfully");
    }


    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadFile(
            @AuthenticationPrincipal User user,
            @RequestParam(value = "path", defaultValue = "") String path,
            ServletRequest servletRequest) throws Exception {
        String fullPath = buildFullPath(user.getId(), path);

        InputStream inputStream = fileService.getFile(fullPath);

        String fileName = path.contains("/") ? path.substring(path.lastIndexOf("/") + 1) : path;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream));
    }

    @DeleteMapping("/deleteFile")
    public ResponseEntity<String> deleteFile(
            @RequestParam(value = "path") String path,
            @AuthenticationPrincipal User user
    ) {
        try {
            fileService.deleteFile(buildFullPath(user.getId(), path));
            return ResponseEntity.ok("Deleted file successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/deleteFolder")
    public ResponseEntity<String> deleteFolder(
            @RequestParam(value = "path", defaultValue = "") String path,
            @AuthenticationPrincipal User user
    ) {
        try {
            fileService.deleteFolder(buildFullPath(user.getId(), path));
            return ResponseEntity.ok("Deleted folder successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    private String buildFullPath(Long userId, String path) {
        String root = "user-" + userId;
        if (path == null || path.isEmpty()) {
            return root;
        }

        String cleanPath = path.startsWith("/") ? path.substring(1) : path;

        if (cleanPath.endsWith("/")) {
            cleanPath = cleanPath.substring(0, cleanPath.length() - 1);
        }
        return root + "/" + cleanPath;
    }


}
