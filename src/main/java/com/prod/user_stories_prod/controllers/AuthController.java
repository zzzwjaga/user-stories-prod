package com.prod.user_stories_prod.controllers;

import com.prod.user_stories_prod.requests.LoginRequest;
import com.prod.user_stories_prod.responses.AuthResponse;
import com.prod.user_stories_prod.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    private final AuthService authService;


    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.authenticate(request.email(), request.password());
        return ResponseEntity.ok(response);
    }
}
