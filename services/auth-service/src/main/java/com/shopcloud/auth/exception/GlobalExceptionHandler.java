package com.shopcloud.auth.exception;

import com.shopcloud.auth.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Bệ đỡ lỗi toàn cục (Global Exception Handler) tập trung cho dịch vụ Authentication (auth-service).
 * <p>
 * SRP (Single Responsibility Principle): Đảm nhận duy nhất nhiệm vụ bắt toàn bộ các ngoại lệ (Exception)
 * bắn ra từ tầng Controller/Service, sau đó chuyển đổi và đóng gói thành chuỗi JSON {@link ErrorResponse} chuẩn hóa.
 */
@RestControllerAdvice // Annotation đánh dấu đây là bệ đỡ xử lý ngoại lệ tập trung cho tất cả các @RestController
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Bắt và xử lý các lỗi nghiệp vụ không tìm thấy tài nguyên (ResourceNotFoundException).
     * Trả về ResponseEntity mã HTTP 404 (Not Found) kèm thông điệp rõ ràng.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Bắt và xử lý các lỗi nghiệp vụ dữ liệu không hợp lệ (BadRequestException, IllegalArgumentException).
     * Trả về ResponseEntity mã HTTP 400 (Bad Request).
     */
    @ExceptionHandler({BadRequestException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBadRequestException(
            Exception ex, HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Bắt và xử lý các lỗi Validation khi dùng annotation @Valid trên dữ liệu đầu vào.
     * Trả về ResponseEntity mã HTTP 400 (Bad Request) kèm chi tiết lỗi vi phạm đầu tiên.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .findFirst()
                .orElse("Dữ liệu gửi lên không hợp lệ!");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(errorMessage)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Bắt tất cả các lỗi hệ thống chưa lường trước (Exception.class).
     * Trả về mã HTTP 500 (Internal Server Error) kèm thông điệp an toàn cho Client,
     * đồng thời dùng Logger ghi log chi tiết stack trace tại console backend cho công tác giám sát (monitoring).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, HttpServletRequest request) {

        // Ghi log chi tiết (stack trace) ra Console Backend phục vụ việc giám sát & khắc phục sự cố
        log.error("[Auth-Service Error] Sự cố hệ thống chưa lường trước tại URI [{}]: ", request.getRequestURI(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("Đã xảy ra lỗi nội bộ hệ thống. Vui lòng thử lại sau hoặc liên hệ bộ phận hỗ trợ!")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
