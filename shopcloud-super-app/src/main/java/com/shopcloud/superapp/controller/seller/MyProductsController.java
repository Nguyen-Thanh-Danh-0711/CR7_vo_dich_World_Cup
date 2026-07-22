package com.shopcloud.superapp.controller.seller;

import com.shopcloud.superapp.model.Product;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller cho màn hình Quản lý sản phẩm của tôi (MyProductsView).
 * <p>
 * Trách nhiệm theo SRP: Hiển thị danh sách sản phẩm của người bán trong TableView,
 * cung cấp thao tác Xem chi tiết (mở SellerProductDetailModal), Sửa giá, và Xóa sản phẩm.
 * Dữ liệu được bind trực tiếp từ {@link SellerProductStore} — mọi thay đổi
 * từ AddProductController sẽ tự động phản ánh lên TableView.
 */
public class MyProductsController implements Initializable {

    // --- @FXML bindings: ánh xạ với MyProductsView.fxml ---

    @FXML
    private TableView<Product> productTable;

    @FXML
    private TableColumn<Product, String> colProductId;

    @FXML
    private TableColumn<Product, String> colProductName;

    @FXML
    private TableColumn<Product, String> colProductPrice;

    @FXML
    private TableColumn<Product, String> colProductStock;

    @FXML
    private TableColumn<Product, String> colProductStatus;

    @FXML
    private TableColumn<Product, Void> colActions;

    @FXML
    private Label lblProductCount;

    /** Bộ định dạng tiền tệ VNĐ cho cột giá. */
    private final NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

    /** Kho dữ liệu chung chia sẻ với AddProductController. */
    private final SellerProductStore productStore = SellerProductStore.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Cấu hình các cột dữ liệu của TableView
        configureTableColumns();

        // 2. Cấu hình cột Thao tác với các nút bấm
        configureActionsColumn();

        // 3. Bind dữ liệu từ SellerProductStore vào TableView
        productTable.setItems(productStore.getProducts());

        // 4. Lắng nghe thay đổi danh sách để cập nhật label đếm sản phẩm
        updateProductCountLabel();
        productStore.getProducts().addListener((ListChangeListener<Product>) change -> updateProductCountLabel());
    }

    // ========================================================================================
    // CẤU HÌNH TABLEVIEW (TABLE CONFIGURATION)
    // ========================================================================================

    /**
     * Ánh xạ các cột TableView với thuộc tính tương ứng của Product model.
     */
    private void configureTableColumns() {
        colProductId.setCellValueFactory(row ->
                new SimpleStringProperty(row.getValue().getId()));

        colProductName.setCellValueFactory(row ->
                new SimpleStringProperty(row.getValue().getName()));

        // Hiển thị giá đã định dạng VNĐ
        colProductPrice.setCellValueFactory(row ->
                new SimpleStringProperty(formatPrice(row.getValue().getPrice())));

        // Hiển thị số lượng tồn kho
        colProductStock.setCellValueFactory(row ->
                new SimpleStringProperty(String.valueOf(row.getValue().getStock())));

        // Hiển thị trạng thái kinh doanh
        if (colProductStatus != null) {
            colProductStatus.setCellValueFactory(row ->
                    new SimpleStringProperty(row.getValue().getStatusText()));
        }
    }

    /**
     * Cấu hình cột Thao tác (Actions) — mỗi dòng hiển thị 3 nút: Xem chi tiết, Sửa giá, Xóa.
     */
    private void configureActionsColumn() {
        colActions.setCellFactory(column -> new TableCell<>() {
            private final Button btnDetail = createActionButton("Chi tiết", "#2563EB", "#FFFFFF");
            private final Button btnEditPrice = createActionButton("Sửa giá", "#F59E0B", "#FFFFFF");
            private final Button btnDelete = createActionButton("Xóa", "#EF4444", "#FFFFFF");
            private final HBox actionBox = new HBox(6, btnDetail, btnEditPrice, btnDelete);

            {
                actionBox.setAlignment(Pos.CENTER);

                btnDetail.setOnAction(event -> {
                    Product product = getTableRow().getItem();
                    if (product != null) {
                        handleViewDetail(product);
                    }
                });

                btnEditPrice.setOnAction(event -> {
                    Product product = getTableRow().getItem();
                    if (product != null) {
                        handleEditPrice(product);
                    }
                });

                btnDelete.setOnAction(event -> {
                    Product product = getTableRow().getItem();
                    if (product != null) {
                        handleDeleteProduct(product);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });
    }

    private Button createActionButton(String text, String bgColor, String textColor) {
        Button btn = new Button(text);
        btn.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: %s; -fx-font-weight: bold; "
                + "-fx-background-radius: 8; -fx-padding: 4 10 4 10; -fx-cursor: hand; -fx-font-size: 11px;",
                bgColor, textColor));
        btn.setMinWidth(70);
        return btn;
    }

    // ========================================================================================
    // THAO TÁC XEM CHI TIẾT SẢN PHẨM DÀNH CHO NGƯỜI BÁN (SELLER PRODUCT DETAIL MODAL)
    // ========================================================================================

    /**
     * Mở Pop-up Modal hiển thị chi tiết sản phẩm chuyên biệt cho Người bán (SellerProductDetailModal.fxml).
     * Loại bỏ các nút mua hàng của Người mua, hỗ trợ thao tác Chỉnh sửa và Ẩn/Tắt kinh doanh.
     *
     * @param product Sản phẩm cần xem chi tiết
     */
    private void handleViewDetail(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/seller/SellerProductDetailModal.fxml"));
            Parent root = loader.load();

            SellerProductDetailController controller = loader.getController();
            controller.setProduct(product, () -> productTable.refresh());

            Stage modalStage = new Stage();
            modalStage.setTitle("Chi tiết sản phẩm (Kênh Người Bán) — " + product.getName());
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setScene(new Scene(root));
            modalStage.setResizable(false);
            modalStage.showAndWait();
        } catch (Exception e) {
            throw new RuntimeException("Không thể mở giao diện chi tiết sản phẩm dành cho Người bán: " + product.getName(), e);
        }
    }

    // ========================================================================================
    // THAO TÁC SỬA GIÁ SẢN PHẨM (EDIT PRICE)
    // ========================================================================================

    /**
     * Mở TextInputDialog cho phép Người bán nhập giá mới.
     * Validate: giá phải là số thực dương, không chấp nhận chữ/ký tự đặc biệt.
     *
     * @param product Sản phẩm cần sửa giá
     */
    private void handleEditPrice(Product product) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf((long) product.getPrice()));
        dialog.setTitle("Chỉnh sửa giá sản phẩm");
        dialog.setHeaderText("Sản phẩm: " + product.getName());
        dialog.setContentText("Nhập giá mới (VNĐ):");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(newPriceText -> {
            // Loại bỏ dấu phân cách hàng nghìn
            String normalized = newPriceText.trim().replace(".", "").replace(",", "");

            // Validate: phải là số dương
            if (!normalized.matches("\\d+(\\.\\d+)?")) {
                throw new IllegalArgumentException(
                        "Giá tiền mới không hợp lệ! Vui lòng nhập số dương (Ví dụ: 890000).");
            }

            double newPrice = Double.parseDouble(normalized);

            if (newPrice <= 0) {
                throw new IllegalArgumentException("Giá tiền mới phải lớn hơn 0!");
            }

            // Cập nhật giá vào Product model
            product.setPrice(newPrice);

            // Refresh TableView để hiển thị giá mới
            productTable.refresh();
        });
    }

    // ========================================================================================
    // THAO TÁC XÓA SẢN PHẨM (DELETE PRODUCT)
    // ========================================================================================

    /**
     * Hiển thị Alert xác nhận trước khi xóa sản phẩm khỏi danh sách.
     * Nếu người dùng chọn YES — xóa khỏi SellerProductStore (tự động cập nhật TableView).
     *
     * @param product Sản phẩm cần xóa
     */
    private void handleDeleteProduct(Product product) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xóa sản phẩm");
        confirmAlert.setHeaderText("Bạn có chắc chắn muốn xóa sản phẩm này?");
        confirmAlert.setContentText("Sản phẩm: " + product.getName() + " (" + product.getId() + ")\n"
                + "Đơn giá: " + formatPrice(product.getPrice()) + "\n\n"
                + "⚠ Hành động này không thể hoàn tác!");

        // Cấu hình 2 nút: Xóa (OK) và Hủy
        confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            productStore.removeProduct(product);
            // Label đếm sản phẩm tự động cập nhật nhờ ListChangeListener
        }
    }

    // ========================================================================================
    // UI HELPERS — Các hàm hỗ trợ giao diện
    // ========================================================================================

    /** Định dạng số tiền theo chuẩn VNĐ (Ví dụ: 890.000đ). */
    private String formatPrice(double price) {
        return currencyFormat.format(price) + "đ";
    }

    /** Cập nhật label hiển thị tổng số sản phẩm. */
    private void updateProductCountLabel() {
        int count = productStore.getProducts().size();
        lblProductCount.setText("Tổng: " + count + " sản phẩm");
    }
}
