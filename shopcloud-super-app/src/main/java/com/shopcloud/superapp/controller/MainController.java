package com.shopcloud.superapp.controller;

import com.shopcloud.superapp.App;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Controller điều hướng Workspace theo phân quyền JWT (ROLE_BUYER / ROLE_SELLER / ROLE_ADMIN).
 * <p>
 * Trách nhiệm theo SRP: Ẩn/hiện menu và nạp FXML không gian tương ứng vào {@link #contentArea}.
 * Không tự bắt lỗi hay hiển thị Alert — mọi ngoại lệ (IO, NPE, Runtime...) được
 * ném ra để {@link com.shopcloud.superapp.exception.GlobalExceptionHandler} xử lý tập trung.
 */
public class MainController implements Initializable {

    private static final String ROLE_BUYER = "ROLE_BUYER";
    private static final String ROLE_SELLER = "ROLE_SELLER";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    @FXML
    private Button btnBuyerSpace;

    @FXML
    private Button btnSellerSpace;

    @FXML
    private Button btnAdminSpace;

    @FXML
    private StackPane contentArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Lấy danh sách roles từ session toàn cục sau đăng nhập (mock JWT)
        Set<String> roles = App.UserSession.getRoles();
        
        // 2. Phân quyền hiển thị các nút điều hướng trên thanh Side Menu
        applyRoleBasedMenuVisibility(roles);

        // 3. Tự động nạp "Không gian Người Mua" làm màn hình mặc định ban đầu
        try {
            loadBuyerSpace();
        } catch (IOException e) {
            // Chuyển đổi thành RuntimeException để tự động "bắn" lỗi lên bệ đỡ lỗi toàn cục
            // (GlobalExceptionHandler) xử lý tập trung, giữ cho code initialize() luôn sạch.
            throw new RuntimeException("Không thể tự động nạp giao diện Không gian Người Mua mặc định!", e);
        }
    }

    /**
     * Phân quyền hiển thị menu theo mô hình phân tầng chức năng ShopCloud.
     */
    private void applyRoleBasedMenuVisibility(Set<String> roles) {
        // Không gian Người mua: luôn luôn hiển thị với mọi tài khoản đăng nhập
        setNodeVisibility(btnBuyerSpace, true);

        // Kênh Người bán: hiển thị nếu đã kích hoạt Shop (ROLE_SELLER) hoặc Admin có quyền giám sát
        boolean showSellerSpace = roles.contains(ROLE_SELLER) || roles.contains(ROLE_ADMIN);
        setNodeVisibility(btnSellerSpace, showSellerSpace);

        // Không gian Admin: tuyệt đối ẩn với mọi tài khoản thường — chỉ hiển thị cho ROLE_ADMIN nội bộ
        boolean showAdminSpace = roles.contains(ROLE_ADMIN);
        setNodeVisibility(btnAdminSpace, showAdminSpace);
    }

    /**
     * Ẩn/hiện một Node trên layout: Sử dụng cả visible và managed 
     * để phần tử biến mất hoàn toàn và không chiếm chỗ trống trong luồng sắp xếp.
     */
    private void setNodeVisibility(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    @FXML
    private void loadBuyerSpace() throws IOException {
        loadWorkspace("/fxml/buyer/BuyerSpace.fxml");
    }

    @FXML
    private void loadSellerSpace() throws IOException {
        loadWorkspace("/fxml/seller/SellerSpace.fxml");
    }

    @FXML
    private void loadAdminSpace() throws IOException {
        loadWorkspace("/fxml/admin/AdminSpace.fxml");
    }

    /**
     * Nạp FXML workspace bất kỳ và thay thế vào vùng hiển thị trung tâm.
     * <p>
     * CẬP NHẬT: Thay đổi kiểu dữ liệu nạp từ 'Pane' sang 'Parent' để tương thích toàn diện 
     * với cả cấu trúc layout gốc kiểu Control phức tạp (ví dụ: SplitPane trong Kênh Người Bán).
     * Ném IOException nếu sai đường dẫn file — GlobalExceptionHandler sẽ tự động bắt lấy.
     */
    private void loadWorkspace(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        // Đã đổi sang kiểu dữ liệu Parent để bảo đảm tính đa hình khi nạp thẻ Root
        Parent workspace = loader.load(); 
        contentArea.getChildren().setAll(workspace);
    }
}
