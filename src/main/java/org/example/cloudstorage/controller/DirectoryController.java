package org.example.cloudstorage.controller;

import org.example.cloudstorage.dto.StorageObjectDto;
import org.example.cloudstorage.entity.User;
import org.example.cloudstorage.service.FileService;
import org.example.cloudstorage.util.PathUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
public class DirectoryController {
    private final FileService fileService;

    public DirectoryController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping
    public List<StorageObjectDto> getDirectory(
            @RequestParam(defaultValue = "") String path,
            @AuthenticationPrincipal User user
    ) {
        return fileService.getFiles(PathUtil.buildFullPath(user.getId(), path));
    }


    @PostMapping
    public ResponseEntity<Void> createFolder(
            @RequestParam String path,
            @AuthenticationPrincipal User user
    ) {
        fileService.createFolder(PathUtil.buildFullPath(user.getId(), path));
        return ResponseEntity.ok().build();
    }


}
