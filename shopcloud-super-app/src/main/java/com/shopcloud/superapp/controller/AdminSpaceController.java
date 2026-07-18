package com.shopcloud.superapp.controller;

import com.shopcloud.superapp.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Controller chịu trách nhiệm quản lý không gian làm việc của Admin (AdminSpace).
 * <p>
 * SRP: Nạp dữ liệu thống kê giả lập ban đầu và tiếp nhận yêu cầu kích hoạt chiến dịch khẩn cấp.
 * Tuân thủ SRP: Không bắt lỗi tại chỗ hay tự ý hiển thị giao diện thông báo lỗi (Alert/Dialog).
 * Toàn bộ ngoại lệ sẽ được ủy thác (throws Exception) cho GlobalExceptionHandler xử lý tập trung.
 */
public class AdminSpaceController implements Initializable {

    // --- Các thành phần giao diện ánh xạ từ AdminSpace.fxml thông qua fx:id ---

    @FXML
    private Label totalOrdersLabel;

    @FXML
    private Label activeShopsLabel;

    @FXML
    private Button btnTriggerFlashSale;

    /**
     * Khởi tạo giao diện AdminSpace.
     * Gán các con số thống kê giả lập (Mock Dashboard Data) định dạng hiển thị chuẩn mực.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadMockDashboardData();
    }

    /**
     * Nạp dữ liệu giả lập thống kê lên các Label hiển thị.
     * Sử dụng định dạng số chuẩn (phân cách hàng nghìn bằng dấu phẩy) tránh hiển thị số thô.
     */
    private void loadMockDashboardData() {
        // Dữ liệu giả lập
        long mockTotalOrders = 1542;
        long mockActiveShops = 89;

        // Sử dụng bộ định dạng Locale US để hiển thị phân cách bằng dấu phẩy (ví dụ: 1,542)
        NumberFormat numberFormatter = NumberFormat.getNumberInstance(Locale.US);

        totalOrdersLabel.setText(numberFormatter.format(mockTotalOrders));
        activeShopsLabel.setText(numberFormatter.format(mockActiveShops));
    }

    /**
     * Xử lý hành động khi Admin click nút "KÍCH HOẠT FLASH SALE".
     * Tuân thủ tuyệt đối quy tắc phân quyền và SRP:
     * - Kiểm tra quyền hạn "ROLE_ADMIN" từ App.UserSession.
     * - Giả lập tiến trình kích hoạt Locust kiểm thử tải.
     * - Ném trực tiếp ngoại lệ (IllegalStateException) nếu có lỗi xảy ra hoặc sai quyền,
     *   để GlobalExceptionHandler xử lý tập trung, tuyệt đối không dùng try-catch cục bộ.
     *
     * @param event Sự kiện ActionEvent từ JavaFX
     * @throws Exception Các lỗi phát sinh trong luồng xử lý hoặc lỗi phân quyền
     */
    @FXML
    private void handleKichHoatFlashSale(ActionEvent event) throws Exception {
        // 1. Kiểm tra phân quyền an toàn từ UserSession toàn cục
        Set<String> roles = App.UserSession.getRoles();
        if (roles == null || !roles.contains("ROLE_ADMIN")) {
            throw new IllegalStateException("Từ chối truy cập: Bạn không có quyền quản trị!");
        }

        // 2. Giả lập tiến trình kiểm thử tải (mô phỏng Locust Script gửi tín hiệu kích hoạt)
        System.out.println("[Admin] Đang gửi tín hiệu kích hoạt Python Locust Script ném tải lên cụm máy chủ AWS EC2...");
        
        // Mô phỏng log thành công xuống console hoặc có thể lưu vết lịch sử hệ thống
        System.out.println("[Admin] Tín hiệu kích hoạt Flash Sale toàn hệ thống đã được phát đi thành công!");
    }
}
