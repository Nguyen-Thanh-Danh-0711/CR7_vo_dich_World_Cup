package com.shopcloud.order.exception;

/**
 * Ngoại lệ nghiệp vụ phát sinh khi yêu cầu gửi lên không hợp lệ (HTTP Status 400 - Bad Request).
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
