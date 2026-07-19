package com.shopcloud.order.exception;

/**
 * Ngoại lệ nghiệp vụ phát sinh khi người mua này cố tình truy cập hoặc chỉnh sửa thông tin
 * đơn hàng của người mua khác thông qua việc thay đổi ID trên đường dẫn (HTTP Status 403 - Forbidden).
 */
public class UnauthorizedOrderAccessException extends RuntimeException {
    public UnauthorizedOrderAccessException(String message) {
        super(message);
    }
}
