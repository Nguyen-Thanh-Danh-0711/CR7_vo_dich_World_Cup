package com.shopcloud.auth.security;

import com.shopcloud.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey; // Khóa bí mật đê ký xác minh JWT
    // Chỉ server biết khóa này
    private final long jwtExpirationInMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.expiration-ms}") long jwtExpirationInMs) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationInMs = jwtExpirationInMs;
    }

    public String generateToken(User user) { // Hàm tạo JWT
        Date now = new Date(); // Thời điểm hiện tại
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs); // THời điểm hết hạn

        return Jwts.builder() // Builder tạo JWT
                .subject(user.getUsername()) // Định danh token
                .claim("roles", user.getRoles())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey) // Server dùng secretkey để ký token
                .compact(); // Ghép Header + Payload + Signature thành chuỗi JWT (eyJhbGciOiJIUzI1NiJ9...)
    }

    public String getUsernameFromJWT(String token) { // Đọc username
        Claims claims = Jwts.parser()
                .verifyWith(secretKey) // Dùng secret để xác minh chứ kỹ
                // Nếu thay đổi ném exception
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public boolean validateToken(String token) { // Kiểm tra token
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }
}
