package com.shopcloud.auth.exception;

/**
 * Ngoại lệ nghiệp vụ phát sinh khi đăng ký tài khoản với username/email đã tồn tại (HTTP Status 400 - Bad Request).
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
