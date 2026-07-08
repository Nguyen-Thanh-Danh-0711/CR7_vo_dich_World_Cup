package com.shopcloud.auth.controller;

import com.shopcloud.auth.dto.AuthResponse;
import com.shopcloud.auth.dto.LoginRequest;
import com.shopcloud.auth.dto.RegisterRequest;
import com.shopcloud.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
// Có thể nhận request HTTP
// Giá trị trả về sẽ được tự động chuyển thành JSON
@RequestMapping("/api/auth") // Đường dẫn chung controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    } // Tạo Authservice truyền vào constructor

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest) {
        AuthResponse authResponse = authService.register(registerRequest); // Chuyển dữ liệu sang service
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
        // Mã trạng thái phù hợp khi tạo mới tài khoản
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);
        return ResponseEntity.ok(authResponse);
    }
}
