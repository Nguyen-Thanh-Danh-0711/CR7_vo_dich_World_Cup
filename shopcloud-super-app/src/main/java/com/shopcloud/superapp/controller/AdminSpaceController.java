package com.shopcloud.superapp.controller;

import com.shopcloud.superapp.App;
import com.shopcloud.superapp.model.Product;
import com.shopcloud.superapp.model.Shop;
import com.shopcloud.superapp.model.User;
import com.shopcloud.superapp.store.AdminDataStore;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Controller chịu trách nhiệm điều khiển Phân Hệ Quản Trị Admin (Admin Space).
 * <p>
 * Trách nhiệm theo SRP:
 * 1. Quản lý Thống kê Dashboard & Trigger Flash Sale khẩn cấp.
 * 2. Điều khiển các Tab Quản lý Người dùng, Quản lý Shop, Quản lý Sản phẩm theo Shop và nhúng Trung tâm Báo cáo Vi phạm.
 * 3. Xử lý các thao tác Khóa/Mở khóa User, Shop, Xóa sản phẩm và mở Modal Gửi cảnh báo vi phạm.
 * Mọi ngoại lệ được ném ra để {@link com.shopcloud.superapp.exception.GlobalExceptionHandler} xử lý tập trung.
 */
public class AdminSpaceController implements Initializable {

    // --- DASHBOARD CONTROLS ---
    @FXML
    private Label totalOrdersLabel;

    @FXML
    private Label activeShopsLabel;

    @FXML
    private Label pendingReportsLabel;

    @FXML
    private Button btnTriggerFlashSale;

    @FXML
    private TabPane mainTabPane;

    // --- TAB 1: USER MANAGEMENT ---
    @FXML
    private TextField userSearchField;

    @FXML
    private TableView<User> userTableView;

    @FXML
    private TableColumn<User, String> colUserId;

    @FXML
    private TableColumn<User, String> colUsername;

    @FXML
    private TableColumn<User, String> colUserEmail;

    @FXML
    private TableColumn<User, String> colUserRole;

    @FXML
    private TableColumn<User, String> colUserStatus;

    @FXML
    private Button btnToggleBanUser;

    @FXML
    private Button btnSendUserWarning;

    // --- TAB 2: SHOP MANAGEMENT ---
    @FXML
    private TextField shopSearchField;

    @FXML
    private TableView<Shop> shopTableView;

    @FXML
    private TableColumn<Shop, String> colShopId;

    @FXML
    private TableColumn<Shop, String> colShopName;

    @FXML
    private TableColumn<Shop, String> colShopOwner;

    @FXML
    private TableColumn<Shop, String> colShopJoinedDate;

    @FXML
    private TableColumn<Shop, Integer> colShopTotalProducts;

    @FXML
    private TableColumn<Shop, String> colShopStatus;

    @FXML
    private Button btnToggleBanShop;

    @FXML
    private Button btnSendShopWarning;

    // --- TAB 3: PRODUCT MANAGEMENT BY SHOP ---
    @FXML
    private ComboBox<Shop> shopSelectComboBox;

    @FXML
    private TableView<Product> productTableView;

    @FXML
    private TableColumn<Product, String> colProdId;

    @FXML
    private TableColumn<Product, String> colProdName;

    @FXML
    private TableColumn<Product, String> colProdPrice;

    @FXML
    private TableColumn<Product, Integer> colProdStock;

    @FXML
    private TableColumn<Product, String> colProdSold;

    @FXML
    private TableColumn<Product, String> colProdShopName;

    @FXML
    private TableColumn<Product, String> colProdStatus;

    @FXML
    private Button btnDeleteProductViolation;

    @FXML
    private Button btnSendProductWarning;

    // --- TAB 4: INTEGRATED REPORT VIEW CONTAINER ---
    @FXML
    private VBox reportContainer;

    private FilteredList<User> filteredUsers;
    private FilteredList<Shop> filteredShops;
    private FilteredList<Product> filteredProducts;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadMockDashboardData();

        setupUserManagementTab();
        setupShopManagementTab();
        setupProductManagementTab();
        loadEmbeddedReportView();
    }

    private void loadMockDashboardData() {
        long mockTotalOrders = 1542;
        NumberFormat numberFormatter = NumberFormat.getNumberInstance(Locale.US);

        totalOrdersLabel.setText(numberFormatter.format(mockTotalOrders));
        activeShopsLabel.setText(String.valueOf(AdminDataStore.getInstance().getShops().stream().filter(s -> !s.isBanned()).count()));

        updatePendingReportsCount();
        AdminDataStore.getInstance().getReports().addListener((javafx.collections.ListChangeListener<com.shopcloud.superapp.model.ViolationReport>) c -> updatePendingReportsCount());
    }

    private void updatePendingReportsCount() {
        long count = AdminDataStore.getInstance().getReports().stream().filter(r -> "PENDING".equalsIgnoreCase(r.getStatus())).count();
        pendingReportsLabel.setText(String.valueOf(count));
    }

    // ========================================================================================
    // TAB A: QUẢN LÝ NGƯỜI DÙNG (USER MANAGEMENT)
    // ========================================================================================
    private void setupUserManagementTab() {
        colUserId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        colUsername.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        colUserEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colUserRole.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));
        colUserStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatusText()));

        filteredUsers = new FilteredList<>(AdminDataStore.getInstance().getUsers(), p -> true);
        userSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String query = newVal != null ? newVal.trim().toLowerCase() : "";
            filteredUsers.setPredicate(user -> query.isEmpty() ||
                    user.getUsername().toLowerCase().contains(query) ||
                    user.getEmail().toLowerCase().contains(query) ||
                    user.getId().toLowerCase().contains(query));
        });

        userTableView.setItems(filteredUsers);
    }

    @FXML
    private void handleToggleBanUser(ActionEvent event) {
        User selected = userTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            throw new IllegalArgumentException("Vui lòng chọn 1 Người dùng từ danh sách!");
        }

        if (selected.isBanned()) {
            AdminDataStore.getInstance().unbanUser(selected.getId());
            showAlert("Thông báo", "Đã mở khóa tài khoản người dùng [" + selected.getUsername() + "].");
        } else {
            AdminDataStore.getInstance().banUser(selected.getId());
            showAlert("Thông báo", "Đã khóa (BAN) tài khoản người dùng [" + selected.getUsername() + "].");
        }
        userTableView.refresh();
    }

    @FXML
    private void handleSendUserWarning(ActionEvent event) {
        User selected = userTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            throw new IllegalArgumentException("Vui lòng chọn 1 Người dùng để gửi cảnh báo!");
        }
        openWarningModal(selected.getId(), selected.getUsername(), "USER");
    }

    // ========================================================================================
    // TAB B: QUẢN LÝ SHOP (SHOP MANAGEMENT)
    // ========================================================================================
    private void setupShopManagementTab() {
        colShopId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        colShopName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colShopOwner.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOwnerName()));
        colShopJoinedDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getJoinedDate()));
        colShopTotalProducts.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getTotalProducts()).asObject());
        colShopStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatusText()));

        filteredShops = new FilteredList<>(AdminDataStore.getInstance().getShops(), p -> true);
        shopSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String query = newVal != null ? newVal.trim().toLowerCase() : "";
            filteredShops.setPredicate(shop -> query.isEmpty() ||
                    shop.getName().toLowerCase().contains(query) ||
                    shop.getId().toLowerCase().contains(query) ||
                    shop.getOwnerName().toLowerCase().contains(query));
        });

        shopTableView.setItems(filteredShops);
    }

    @FXML
    private void handleToggleBanShop(ActionEvent event) {
        Shop selected = shopTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            throw new IllegalArgumentException("Vui lòng chọn 1 Shop từ danh sách!");
        }

        if (selected.isBanned()) {
            AdminDataStore.getInstance().unbanShop(selected.getId());
            showAlert("Thông báo", "Đã mở khóa Cửa hàng [" + selected.getName() + "].");
        } else {
            AdminDataStore.getInstance().banShop(selected.getId());
            showAlert("Thông báo", "Đã khóa (BAN) Cửa hàng [" + selected.getName() + "].");
        }
        shopTableView.refresh();
        activeShopsLabel.setText(String.valueOf(AdminDataStore.getInstance().getShops().stream().filter(s -> !s.isBanned()).count()));
    }

    @FXML
    private void handleSendShopWarning(ActionEvent event) {
        Shop selected = shopTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            throw new IllegalArgumentException("Vui lòng chọn 1 Shop để gửi cảnh báo!");
        }
        openWarningModal(selected.getId(), selected.getName(), "SHOP");
    }

    // ========================================================================================
    // TAB C: QUẢN LÝ SẢN PHẨM THEO SHOP (PRODUCT MANAGEMENT BY SHOP)
    // ========================================================================================
    private void setupProductManagementTab() {
        colProdId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        colProdName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colProdPrice.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedPrice()));
        colProdStock.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getStock()).asObject());
        colProdSold.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedSoldQuantity()));
        colProdShopName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getShopName()));
        colProdStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAdminStatusText()));

        // Populates Shop ComboBox
        shopSelectComboBox.setItems(AdminDataStore.getInstance().getShops());

        filteredProducts = new FilteredList<>(AdminDataStore.getInstance().getProducts(), p -> true);

        shopSelectComboBox.valueProperty().addListener((obs, oldVal, selectedShop) -> {
            if (selectedShop == null) {
                filteredProducts.setPredicate(p -> true);
            } else {
                filteredProducts.setPredicate(p -> p.getShopId().equalsIgnoreCase(selectedShop.getId()));
            }
        });

        productTableView.setItems(filteredProducts);
    }

    @FXML
    private void handleDeleteProductViolation(ActionEvent event) {
        Product selected = productTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            throw new IllegalArgumentException("Vui lòng chọn 1 Sản phẩm để thực hiện Xóa!");
        }

        AdminDataStore.getInstance().deleteProduct(selected.getId());
        productTableView.refresh();
        showAlert("Thông báo", "Đã gán trạng thái [REMOVED_BY_ADMIN] đối với sản phẩm [" + selected.getName() + "].");
    }

    @FXML
    private void handleSendProductWarning(ActionEvent event) {
        Product selected = productTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            throw new IllegalArgumentException("Vui lòng chọn 1 Sản phẩm để gửi cảnh báo!");
        }
        openWarningModal(selected.getId(), selected.getName(), "PRODUCT");
    }

    // ========================================================================================
    // TAB D: NHÚNG TRUNG TÂM BÁO CÁO VI PHẠM (INTEGRATED REPORT VIEW)
    // ========================================================================================
    private void loadEmbeddedReportView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/AdminReportView.fxml"));
            Parent reportView = loader.load();
            reportContainer.getChildren().setAll(reportView);
        } catch (IOException e) {
            throw new RuntimeException("Không thể nạp Trung tâm báo cáo vi phạm tích hợp!", e);
        }
    }

    // ========================================================================================
    // HELPER MODAL & ALERTS
    // ========================================================================================
    private void openWarningModal(String targetId, String targetName, String targetType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/AdminWarningModal.fxml"));
            Parent root = loader.load();

            AdminWarningModalController controller = loader.getController();
            controller.setTargetData(targetId, targetName, targetType);

            Stage modalStage = new Stage();
            modalStage.setTitle("Gửi Cảnh Báo Vi Phạm");
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setScene(new Scene(root));
            modalStage.setResizable(false);
            modalStage.showAndWait();

        } catch (IOException e) {
            throw new RuntimeException("Không thể nạp Modal Gửi Cảnh Báo!", e);
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleKichHoatFlashSale(ActionEvent event) throws Exception {
        Set<String> roles = App.UserSession.getRoles();
        if (roles == null || !roles.contains("ROLE_ADMIN")) {
            throw new IllegalStateException("Từ chối truy cập: Bạn không có quyền quản trị!");
        }

        System.out.println("[Admin] Đang phát tín hiệu Kích hoạt Flash Sale toàn hệ thống tới cụm máy chủ...");
        showAlert("Flash Sale", "Tín hiệu Kích Hoạt Flash Sale Toàn Hệ Thống đã được gửi thành công!");
    }
}
