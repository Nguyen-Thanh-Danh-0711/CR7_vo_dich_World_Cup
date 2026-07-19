package com.shopcloud.auth.exception;

/**
 * Ngoại lệ nghiệp vụ phát sinh khi sai tài khoản hoặc mật khẩu (HTTP Status 401 - Unauthorized).
 */
public class BadCredentialsException extends RuntimeException {
    public BadCredentialsException(String message) {
        super(message);
    }
}
