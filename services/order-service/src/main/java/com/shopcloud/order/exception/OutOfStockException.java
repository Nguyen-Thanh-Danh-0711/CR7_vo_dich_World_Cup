package com.shopcloud.order.exception;

/**
 * Ngoại lệ nghiệp vụ phát sinh khi khách hàng bấm mua sản phẩm nhưng số lượng trong kho đã hết (HTTP Status 400 - Bad Request).
 */
public class OutOfStockException extends RuntimeException {
    public OutOfStockException(String message) {
        super(message);
    }
}
