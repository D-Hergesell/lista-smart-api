package com.listasmart.api.controller;

import com.listasmart.api.dto.AuthDtos.AuthResponse;
import com.listasmart.api.dto.AuthDtos.Credentials;
import com.listasmart.api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody Credentials body) {
        return auth.register(body);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody Credentials body) {
        return auth.login(body);
    }
}
