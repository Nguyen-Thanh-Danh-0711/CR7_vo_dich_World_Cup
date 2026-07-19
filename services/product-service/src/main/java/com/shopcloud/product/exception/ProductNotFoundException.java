package com.shopcloud.product.exception;

/**
 * Ngoại lệ nghiệp vụ phát sinh khi người dùng tìm kiếm, xem hoặc mua một sản phẩm
 * có ID không tồn tại hoặc đã bị xóa khỏi hệ thống (HTTP Status 404 - Not Found).
 */
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}
