package org.example.cloudstorage.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDto { 
    private String username;

    public UserResponseDto(String username) {
        this.username = username;
    }
}
