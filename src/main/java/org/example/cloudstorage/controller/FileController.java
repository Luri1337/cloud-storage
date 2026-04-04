package org.example.cloudstorage.controller;

import org.example.cloudstorage.entity.User;
import org.example.cloudstorage.service.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

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
            StringBuilder prefix = new StringBuilder("user-" + user.getId());

            StringBuilder fullPath = prefix.append(path.isEmpty() ? "" : "/" + path);

            fileService.upload(fullPath.toString(), file);
            return ResponseEntity.ok("Uploaded file successfully");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
