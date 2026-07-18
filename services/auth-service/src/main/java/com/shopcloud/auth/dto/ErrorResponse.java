package com.shopcloud.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Cấu trúc đại diện cho đối tượng phản hồi lỗi đồng nhất (Error Response Schema)
 * trả về cho phía Client (JavaFX / Mobile / Web App).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /** Mốc thời gian hệ thống xảy ra sự cố */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /** Mã trạng thái HTTP (HTTP Status Code: 400, 404, 500, ...) */
    private int status;

    /** Tiêu đề/mô tả ngắn gọn loại lỗi (ví dụ: Bad Request, Not Found, Internal Server Error) */
    private String error;

    /** Thông điệp giải thích chi tiết nguyên nhân gây ra lỗi */
    private String message;

    /** Đường dẫn API (URI path) xảy ra lỗi */
    private String path;
}
