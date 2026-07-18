package com.shopcloud.auth.exception;

/**
 * Ngoại lệ nghiệp vụ phát sinh khi không tìm thấy tài nguyên được yêu cầu (HTTP Status 404 - Not Found).
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
