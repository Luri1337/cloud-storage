package org.example.cloudstorage.dto;

public record StorageObjectDto(
        String name,
        String path,
        Long size,
        String type
) {}

