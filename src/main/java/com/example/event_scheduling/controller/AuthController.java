package com.example.event_scheduling.controller;

import com.example.event_scheduling.dto.ApiResponse;
import com.example.event_scheduling.dto.LoginRequest;
import com.example.event_scheduling.dto.SignupRequest;
import com.example.event_scheduling.model.User;
import com.example.event_scheduling.security.JwtUtil;
import com.example.event_scheduling.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwt;
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    public AuthController(AuthService authService,
                          JwtUtil jwt) {
        this.authService = authService;
        this.jwt = jwt;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> signup(@RequestBody SignupRequest request) {
        logger.info("Currently attempting a signup request for {}", request);
        ApiResponse<String> response = new ApiResponse<String>();
        User user = authService.signup(request.getEmail(), request.getFirstName(), request.getLastName(), request.getPassword());

        if (user == null) {
            response.message = "Email already exists";
            return ResponseEntity.badRequest().body(response);
        }

        String token = jwt.generateToken(user.getEmail());
        response.message = "Successfully created user!";
        response.data = token;
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginRequest request) {
        ApiResponse<String> response = new ApiResponse<String>();
        User user = authService.authenticate(request.getEmail(), request.getPassword());
        if (user == null) {
            response.message = "Invalid credentials";
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String token = jwt.generateToken(user.getEmail());
        response.message = "User successfully logged in!";
        response.data = token;
        return ResponseEntity.ok(response);
    }
}
