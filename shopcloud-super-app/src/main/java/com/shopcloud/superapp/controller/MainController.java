package com.shopcloud.superapp.controller;

import com.shopcloud.superapp.App;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;

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
        // Lấy roles từ session toàn cục (mock JWT sau đăng nhập)
        Set<String> roles = App.UserSession.getRoles();
        applyRoleBasedMenuVisibility(roles);
    }

    /**
     * Phân quyền hiển thị menu theo mô hình Shopee + Admin nội bộ ShopCloud.
     */
    private void applyRoleBasedMenuVisibility(Set<String> roles) {
        // Không gian Người mua: luôn hiển thị — ROLE_BUYER là quyền mặc định mọi tài khoản
        setNodeVisibility(btnBuyerSpace, true);

        // Kênh Người bán: chỉ khi đã "Mở Shop" (ROLE_SELLER) hoặc Admin có quyền giám sát
        boolean showSellerSpace = roles.contains(ROLE_SELLER) || roles.contains(ROLE_ADMIN);
        setNodeVisibility(btnSellerSpace, showSellerSpace);

        // Không gian Admin: tuyệt đối ẩn với tài khoản thường — chỉ ROLE_ADMIN nội bộ
        boolean showAdminSpace = roles.contains(ROLE_ADMIN);
        setNodeVisibility(btnAdminSpace, showAdminSpace);
    }

    /**
     * Ẩn/hiện node trên layout: dùng cả visible và managed để node không chiếm chỗ khi ẩn.
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
     * Nạp FXML workspace và thay thế nội dung vùng trung tâm.
     * Ném IOException nếu đường dẫn sai — GlobalExceptionHandler sẽ bắt và hiển thị Alert.
     */
    private void loadWorkspace(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Pane workspace = loader.load();
        contentArea.getChildren().setAll(workspace);
    }
}
