package main.java.com.shopcloud.superapp.controller;

import com.shopcloud.superapp.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Set;

/**
 * Controller quản lý giao diện Đăng nhập (LoginView).
 * <p>
 * Trách nhiệm theo SRP: Tiếp nhận thông tin đăng nhập, thiết lập UserSession toàn cục
 * và thực hiện chuyển cảnh sang giao diện chính (MainView).
 * Không xử lý ngoại lệ tại chỗ — đẩy toàn bộ lỗi lên GlobalExceptionHandler.
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    /**
     * Xử lý sự kiện khi người dùng bấm nút "Đăng nhập".
     * Khai báo "throws Exception" để đẩy mọi lỗi phát sinh lên hệ thống xử lý tập trung.
     */
    @FXML
    private void handleLogin(ActionEvent event) throws Exception {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // 1. Kiểm tra validate cơ bản (Nếu trống sẽ ném ngoại lệ để Global Handler bắt lấy)
        if (username.isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập và mật khẩu không được để trống!");
        }

        // 2. Cơ chế Mock Data phân quyền linh hoạt theo Username để dễ test:
        Set<String> roles;
        if ("admin_ops".equals(username)) {
            // Kịch bản 3: Admin hệ thống — hiển thị đủ cả 3 không gian
            roles = Set.of("ROLE_BUYER", "ROLE_SELLER", "ROLE_ADMIN");
        } else if ("shop_cr7_official".equals(username)) {
            // Kịch bản 2: Người bán (đã mở shop) — có không gian Buyer & Seller
            roles = Set.of("ROLE_BUYER", "ROLE_SELLER");
        } else {
            // Kịch bản 1: Tài khoản người mua thông thường hoặc bất kỳ username khác
            roles = Set.of("ROLE_BUYER");
        }

        // 3. Khởi tạo session toàn cục lưu vào App.UserSession
        App.UserSession.init(username, roles);

        // 4. Tải giao diện chính (MainView.fxml) sau khi đăng nhập thành công
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        Parent mainViewRoot = loader.load();

        // 5. Lấy Stage hiện tại từ sự kiện click nút và thực hiện chuyển cảnh
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        currentStage.setTitle("ShopCloud Super App — " + App.UserSession.getUsername());
        currentStage.getScene().setRoot(mainViewRoot);
    }
}
