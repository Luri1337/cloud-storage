package org.example.cloudstorage.controller;

import org.example.cloudstorage.UserDto;
import org.example.cloudstorage.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("api/auth/signup")
    public String register(@ModelAttribute UserDto user) {
        userService.registerUser(user);
        return "redirect:/login";
    }


}
