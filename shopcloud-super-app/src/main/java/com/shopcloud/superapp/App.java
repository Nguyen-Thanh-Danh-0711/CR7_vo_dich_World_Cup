package com.shopcloud.superapp;

import com.shopcloud.superapp.exception.GlobalExceptionHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Điểm vào (Entry Point) duy nhất của ShopCloud Super App.
 * <p>
 * Trách nhiệm theo SRP: Quản lý vòng đời khởi chạy JavaFX và duy trì trạng thái
 * {@link UserSession} toàn cục sau khi đăng nhập (mock JWT roles).
 * Không xử lý lỗi tại đây — mọi ngoại lệ không bắt được được ủy quyền cho
 * {@link GlobalExceptionHandler}.
 */
public class App extends Application {

    /**
     * Session toàn cục mô phỏng payload JWT sau đăng nhập.
     * Lưu username và tập quyền (ROLE_BUYER, ROLE_SELLER, ROLE_ADMIN).
     */
    public static final class UserSession {

        private static String username;
        private static Set<String> roles = new HashSet<>();

        private UserSession() {
            // Utility holder — không khởi tạo instance
        }

        /**
         * Khởi tạo session sau đăng nhập thành công (mock hoặc từ Auth Service).
         */
        public static void init(String user, Set<String> userRoles) {
            username = Objects.requireNonNull(user, "username không được null");
            roles = new HashSet<>(Objects.requireNonNull(userRoles, "roles không được null"));
        }

        public static String getUsername() {
            return username;
        }

        /**
         * Trả về bản sao bất biến để tránh sửa trực tiếp tập quyền từ bên ngoài.
         */
        public static Set<String> getRoles() {
            return Collections.unmodifiableSet(roles);
        }

        public static void clear() {
            username = null;
            roles.clear();
        }
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        // ── Mock Data: đổi kịch bản bên dưới để thử phân quyền Workspace ──

        // Kịch bản 1 — Chỉ người mua (mặc định Shopee): menu Seller & Admin bị ẩn
        // UserSession.init("nguyen_van_a", Set.of("ROLE_BUYER"));

        // Kịch bản 2 — Người bán đã "Mở Shop": hiện Kênh Người Bán, ẩn Admin
        UserSession.init("shop_cr7_official", Set.of("ROLE_BUYER", "ROLE_SELLER"));

        // Kịch bản 3 — Nhân viên vận hành ShopCloud: hiện đủ 3 không gian
        // UserSession.init("admin_ops", Set.of("ROLE_BUYER", "ROLE_SELLER", "ROLE_ADMIN"));

        // Nạp layout chính — controller sẽ đọc UserSession.getRoles() trong initialize()
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("ShopCloud Super App — " + UserSession.getUsername());
        
        // --- ĐOẠN ĐƯỢC CẬP NHẬT ---
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        
        // Đặt kích thước tối thiểu để tránh người dùng kéo cửa sổ quá nhỏ làm vỡ/teo layout
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(640);
        
        // Ép cửa sổ mở toàn màn hình (Maximize) ngay khi khởi chạy
        primaryStage.setMaximized(true);
        // --------------------------

        primaryStage.show();
    }

    /**
     * main() chỉ đăng ký bệ đỡ lỗi toàn cục rồi khởi chạy JavaFX.
     * Không chứa logic nghiệp vụ hay try-catch xử lý lỗi.
     */
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());
        launch(args);
    }
}
