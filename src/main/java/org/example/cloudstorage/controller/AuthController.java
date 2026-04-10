package org.example.cloudstorage.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.cloudstorage.dto.UserDto;
import org.example.cloudstorage.dto.UserResponseDto;
import org.example.cloudstorage.entity.User;
import org.example.cloudstorage.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;;

    public AuthController(
            UserService userService,
            AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto signUp(@Valid @RequestBody UserDto request,
                                  HttpServletRequest servletRequest) {

        User user =  userService.registerUser(request);

        authenticateAndCreateSession(
                request.getUsername(),
                request.getPassword(),
                servletRequest
        );

        return new UserResponseDto(user.getUsername());

    }

    @PostMapping("/sign-in")
    public UserResponseDto signIn(@Valid @RequestBody UserDto request,
                                    HttpServletRequest servletRequest) {

        authenticateAndCreateSession(
                request.getUsername(),
                request.getPassword(),
                servletRequest
        );

        return new UserResponseDto(request.getUsername());

    }

    private void authenticateAndCreateSession(
            String username,
            String password,
            HttpServletRequest request)
    {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        HttpSession session = request.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );
    }



}
