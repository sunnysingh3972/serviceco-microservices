package com.serviceco.serviceco_auth_service.controller;

import com.serviceco.serviceco_auth_service.model.dto.LoginRequest;
import com.serviceco.serviceco_auth_service.model.dto.LoginResponse;
import com.serviceco.serviceco_auth_service.model.dto.RegistrationRequest;
import com.serviceco.serviceco_auth_service.model.dto.RegistrationResponse;
import com.serviceco.serviceco_auth_service.services.AuthenticationService;
import com.serviceco.serviceco_auth_service.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse register(
            @Valid @RequestBody RegistrationRequest request) {

        return userService.register(request);
    }
    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request) {

        return authenticationService.login(request);
    }
}