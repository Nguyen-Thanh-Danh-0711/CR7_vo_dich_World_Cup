package com.shopcloud.superapp.controller;

import com.shopcloud.superapp.App;
import com.shopcloud.superapp.util.WindowResizeHelper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Controller chính điều hướng Workspace và quản lý tương tác cửa sổ Custom Stage.
 * <p>
 * Trách nhiệm theo SRP:
 * 1. Ẩn/hiện menu và nạp FXML không gian tương ứng vào {@link #contentArea}
 *    theo phân quyền JWT (ROLE_BUYER / ROLE_SELLER / ROLE_ADMIN).
 * 2. Quản lý sub-menu Kênh Người Bán (toggle Đăng bán / Sản phẩm của tôi).
 * 3. Quản lý tương tác cửa sổ tùy chỉnh (Resize 8 hướng, Phóng to/Thu nhỏ, Ẩn/Đóng cửa sổ).
 * <p>
 * Không tự bắt lỗi hay hiển thị Alert — mọi ngoại lệ (IO, NPE, Runtime...) được
 * ném ra để {@link com.shopcloud.superapp.exception.GlobalExceptionHandler} xử lý tập trung.
 */
public class MainController implements Initializable {

    private static final String ROLE_BUYER = "ROLE_BUYER";
    private static final String ROLE_SELLER = "ROLE_SELLER";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    // ========================================================================================
    // BIẾN LƯU TRỮ TRẠNG THÁI CỬA SỔ — dùng cho cơ chế PHÓNG TO / KHÔI PHỤC (MAXIMIZE / RESTORE)
    // ========================================================================================

    /**
     * Tọa độ X vị trí cửa sổ tại trạng thái kích thước tùy chỉnh gần nhất trước khi phóng to.
     */
    private double lastX;

    /**
     * Tọa độ Y vị trí cửa sổ tại trạng thái kích thước tùy chỉnh gần nhất trước khi phóng to.
     */
    private double lastY;

    /**
     * Chiều rộng (Width) cửa sổ trước khi bấm Phóng to.
     * Mặc định ban đầu: 1024px (khớp với giá trị khởi tạo tại App.java).
     */
    private double lastWidth = 1024;

    /**
     * Chiều cao (Height) cửa sổ trước khi bấm Phóng to.
     * Mặc định ban đầu: 640px (khớp với giá trị khởi tạo tại App.java).
     */
    private double lastHeight = 640;

    /**
     * Cờ đánh dấu trạng thái hiện tại:
     * true = cửa sổ đang phóng to toàn màn hình, false = kích thước bình thường.
     */
    private boolean isMaximized = false;

    // ========================================================================================
    // FXML CONTROLS — ánh xạ với các thành phần giao diện trong MainView.fxml
    // ========================================================================================

    /** Thanh Header Bar tùy chỉnh — cho phép kéo rê di chuyển cửa sổ */
    @FXML
    private HBox headerBar;

    /** Nút Thu nhỏ cửa sổ xuống Taskbar */
    @FXML
    private Button btnMinimize;

    /** Nút Phóng to / Khôi phục kích thước cửa sổ */
    @FXML
    private Button btnMaximize;

    /** Nút Đóng cửa sổ ứng dụng */
    @FXML
    private Button btnCloseWindow;

    @FXML
    private Button btnBuyerSpace;

    @FXML
    private Button btnSellerSpace;

    @FXML
    private Button btnAdminSpace;

    /** Sub-menu Kênh Người Bán — chứa 2 nút con: Đăng bán & Sản phẩm của tôi */
    @FXML
    private VBox sellerSubMenu;

    /** Nút chuyển sang màn hình Đăng bán sản phẩm mới */
    @FXML
    private Button btnAddProduct;

    /** Nút chuyển sang màn hình Sản phẩm của tôi */
    @FXML
    private Button btnMyProducts;

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

        // 4. Đăng ký tính năng Tương tác Cửa sổ tùy chỉnh (Resize 8 hướng + Kéo Header di chuyển)
        setupCustomWindowInteractions();
    }

    // ========================================================================================
    // QUẢN LÝ TƯƠNG TÁC CỬA SỔ TÙY CHỈNH (CUSTOM WINDOW INTERACTIONS)
    // ========================================================================================

    /**
     * Khởi tạo các listener cho Custom Undecorated Window Stage.
     * <p>
     * Sử dụng Platform.runLater() để đảm bảo Scene và Stage đã render hoàn chỉnh
     * trước khi gắn Listener — tránh NullPointerException trên một số hệ điều hành
     * khi cửa sổ chưa kịp vẽ xong (đặc biệt rõ trên WSLg/WSL2).
     */
    private void setupCustomWindowInteractions() {
        Platform.runLater(() -> {
            Stage stage = getPrimaryStage();
            if (stage == null) {
                return;
            }

            // Đảm bảo giới hạn kích thước tối thiểu ban đầu
            stage.setMinWidth(1000);
            stage.setMinHeight(650);

            // Kích hoạt WindowResizeHelper cho phép rê chuột vào 8 viền để thay đổi kích thước
            WindowResizeHelper.enableResizing(stage, 1000, 650, 8);

            // Cho phép kéo rê thanh Header Bar để di chuyển cửa sổ
            // và Double-click vào Header Bar để Maximize/Restore
            if (headerBar != null) {
                WindowResizeHelper.makeDraggable(headerBar, stage, () -> handleMaximizeRestore(null));
            }

            // Cập nhật biến lưu trạng thái ban đầu từ kích thước/vị trí thực tế của Stage
            lastX = stage.getX();
            lastY = stage.getY();
            if (stage.getWidth() > 0) {
                lastWidth = stage.getWidth();
            }
            if (stage.getHeight() > 0) {
                lastHeight = stage.getHeight();
            }
            isMaximized = stage.isMaximized();
        });
    }

    /**
     * Xử lý logic Nút Phóng to / Thu nhỏ tùy chỉnh (Maximize / Restore Button) trên Header Bar.
     * <p>
     * TRẠNG THÁI 1 — PHÓNG TO (MAXIMIZE):
     *   Lưu lại vị trí (x, y) và kích thước (width, height) hiện tại vào các biến nhớ
     *   (lastX, lastY, lastWidth, lastHeight). Sau đó dùng Screen.getPrimary().getVisualBounds()
     *   để mở rộng cửa sổ vừa khít màn hình mà không che Taskbar.
     * <p>
     * TRẠNG THÁI 2 — THU NHỎ / KHÔI PHỤC (RESTORE):
     *   Khôi phục cửa sổ trở về ĐÚNG kích thước (lastWidth, lastHeight) và vị trí (lastX, lastY)
     *   mà người dùng đã tùy chỉnh gần nhất.
     *   Tuyệt đối KHÔNG reset về kích thước mặc định ban đầu nếu đã từng kéo giãn cửa sổ.
     *
     * @param event Sự kiện click chuột từ FXML (có thể null nếu gọi từ double-click Header)
     */
    @FXML
    private void handleMaximizeRestore(ActionEvent event) {
        Stage stage = getPrimaryStage();
        if (stage == null) {
            return;
        }

        if (!isMaximized) {
            // === TRẠNG THÁI 1: BẤM PHÓNG TO (MAXIMIZE) ===

            // 1. Lưu lại kích thước và vị trí tùy chỉnh hiện tại của cửa sổ trước khi phóng to
            lastX = stage.getX();
            lastY = stage.getY();
            lastWidth = stage.getWidth();
            lastHeight = stage.getHeight();

            // 2. Phóng to cửa sổ toàn màn hình KHÔNG che Taskbar bằng VisualBounds
            Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
            stage.setX(visualBounds.getMinX());
            stage.setY(visualBounds.getMinY());
            stage.setWidth(visualBounds.getWidth());
            stage.setHeight(visualBounds.getHeight());
            stage.setMaximized(true);

            isMaximized = true;

            // 3. Đổi icon biểu tượng nút từ Maximize (☐) sang Restore (❐)
            if (btnMaximize != null) {
                btnMaximize.setText("❐");
                btnMaximize.setTooltip(new Tooltip("Khôi phục kích thước"));
            }
        } else {
            // === TRẠNG THÁI 2: BẤM THU NHỎ / KHÔI PHỤC (RESTORE) ===

            // 1. Hủy chế độ Maximized native của hệ điều hành
            stage.setMaximized(false);

            // 2. Khôi phục về ĐÚNG vị trí và kích thước đã tùy chỉnh gần nhất
            //    KHÔNG được đưa về kích thước mặc định ban đầu!
            stage.setX(lastX);
            stage.setY(lastY);
            stage.setWidth(lastWidth);
            stage.setHeight(lastHeight);

            isMaximized = false;

            // 3. Đổi icon biểu tượng nút từ Restore (❐) về Maximize (☐)
            if (btnMaximize != null) {
                btnMaximize.setText("☐");
                btnMaximize.setTooltip(new Tooltip("Phóng to toàn màn hình"));
            }
        }
    }

    /**
     * Thu nhỏ cửa sổ xuống Taskbar (Minimize / Iconify).
     */
    @FXML
    private void handleMinimize(ActionEvent event) {
        Stage stage = getPrimaryStage();
        if (stage != null) {
            stage.setIconified(true);
        }
    }

    /**
     * Đóng hoàn toàn cửa sổ ứng dụng ShopCloud Super App.
     */
    @FXML
    private void handleCloseWindow(ActionEvent event) {
        Stage stage = getPrimaryStage();
        if (stage != null) {
            stage.close();
        }
    }

    /**
     * Helper truy xuất đối tượng Primary Stage từ Scene Graph hiện tại.
     *
     * @return Stage chính của ứng dụng, hoặc null nếu chưa sẵn sàng
     */
    private Stage getPrimaryStage() {
        if (contentArea != null && contentArea.getScene() != null) {
            return (Stage) contentArea.getScene().getWindow();
        }
        return null;
    }

    // ========================================================================================
    // PHÂN QUYỀN HIỂN THỊ MENU (ROLE-BASED MENU VISIBILITY)
    // ========================================================================================

    /**
     * Phân quyền hiển thị menu theo mô hình phân tầng chức năng ShopCloud.
     */
    private void applyRoleBasedMenuVisibility(Set<String> roles) {
        // Không gian Người mua: luôn luôn hiển thị với mọi tài khoản đăng nhập
        setNodeVisibility(btnBuyerSpace, true);

        // Kênh Người bán: hiển thị nếu đã kích hoạt Shop (ROLE_SELLER) hoặc Admin có quyền giám sát
        boolean showSellerSpace = roles.contains(ROLE_SELLER) || roles.contains(ROLE_ADMIN);
        setNodeVisibility(btnSellerSpace, showSellerSpace);
        // Sub-menu mặc định ẩn — chỉ hiện khi bấm nút chính "Kênh Người Bán"
        if (sellerSubMenu != null) {
            setNodeVisibility(sellerSubMenu, false);
        }

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

    // ========================================================================================
    // ĐIỀU HƯỚNG SUB-MENU KÊNH NGƯỜI BÁN (SELLER SUB-MENU NAVIGATION)
    // ========================================================================================

    /**
     * Toggle ẩn/hiện sub-menu Kênh Người Bán khi bấm nút chính.
     * Nếu đang ẩn → hiện sub-menu và tự động nạp "Đăng bán sản phẩm" làm mặc định.
     * Nếu đang hiện → ẩn sub-menu.
     */
    @FXML
    private void handleToggleSellerMenu() {
        if (sellerSubMenu == null) {
            return;
        }

        boolean currentlyVisible = sellerSubMenu.isVisible();

        if (!currentlyVisible) {
            // Mở sub-menu và nạp view mặc định (Đăng bán sản phẩm)
            setNodeVisibility(sellerSubMenu, true);
            try {
                loadAddProductView();
            } catch (IOException e) {
                throw new RuntimeException("Không thể nạp giao diện Đăng bán sản phẩm!", e);
            }
        } else {
            // Đóng sub-menu
            setNodeVisibility(sellerSubMenu, false);
        }
    }

    // ========================================================================================
    // NẠP FXML WORKSPACE (WORKSPACE LOADING)
    // ========================================================================================

    @FXML
    private void loadBuyerSpace() throws IOException {
        // Đóng sub-menu Seller khi chuyển sang Buyer Space
        if (sellerSubMenu != null) {
            setNodeVisibility(sellerSubMenu, false);
        }
        loadWorkspace("/fxml/buyer/BuyerSpace.fxml");
    }

    /**
     * Nạp giao diện Đăng bán sản phẩm mới vào vùng hiển thị trung tâm.
     */
    @FXML
    private void loadAddProductView() throws IOException {
        loadWorkspace("/fxml/seller/AddProductView.fxml");
    }

    /**
     * Nạp giao diện Sản phẩm của tôi vào vùng hiển thị trung tâm.
     */
    @FXML
    private void loadMyProductsView() throws IOException {
        loadWorkspace("/fxml/seller/MyProductsView.fxml");
    }

    @FXML
    private void loadAdminSpace() throws IOException {
        // Đóng sub-menu Seller khi chuyển sang Admin Space
        if (sellerSubMenu != null) {
            setNodeVisibility(sellerSubMenu, false);
        }
        loadWorkspace("/fxml/admin/AdminSpace.fxml");
    }

    /**
     * Nạp FXML workspace bất kỳ và thay thế vào vùng hiển thị trung tâm.
     * <p>
     * CẬP NHẬT: Thay đổi kiểu dữ liệu nạp từ 'Pane' sang 'Parent' để tương thích toàn diện 
     * với cả cấu trúc layout gốc kiểu Control phức tạp (ví dụ: ScrollPane trong AddProductView).
     * Ném IOException nếu sai đường dẫn file — GlobalExceptionHandler sẽ tự động bắt lấy.
     */
    private void loadWorkspace(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        // Đã đổi sang kiểu dữ liệu Parent để bảo đảm tính đa hình khi nạp thẻ Root
        Parent workspace = loader.load(); 
        contentArea.getChildren().setAll(workspace);
    }
}
