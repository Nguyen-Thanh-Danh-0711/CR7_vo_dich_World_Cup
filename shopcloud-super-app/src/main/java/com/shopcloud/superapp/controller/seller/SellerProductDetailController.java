package com.shopcloud.superapp.controller.seller;

import com.shopcloud.superapp.model.Product;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;

/**
 * Controller cho Pop-up Xem chi tiết Sản phẩm dành riêng cho Người bán (SellerProductDetailModal).
 * <p>
 * Trách nhiệm theo SRP: Hiển thị toàn bộ dữ liệu nghiệp vụ Người bán (Tồn kho, Mã SP, 
 * Giá lẻ, Đã bán, Bộ sưu tập ảnh chi tiết, Trạng thái kinh doanh) và cung cấp 
 * các nút thao tác nghiệp vụ: Chỉnh sửa sản phẩm, Ẩn/Tắt kinh doanh, và Đóng.
 */
public class SellerProductDetailController {

    private static final String DEFAULT_IMAGE_PATH = "/fxml/assets/logo.png";

    @FXML
    private Label lblHeaderSubtitle;

    @FXML
    private Label lblProductIdBadge;

    @FXML
    private Label lblStatusBadge;

    @FXML
    private ImageView imgMain;

    @FXML
    private HBox galleryContainer;

    @FXML
    private Label lblNoDetailImages;

    @FXML
    private Label lblProductName;

    @FXML
    private Label lblProductPrice;

    @FXML
    private Label lblStockQuantity;

    @FXML
    private Label lblSoldQuantity;

    @FXML
    private Label lblRating;

    @FXML
    private Label lblDescription;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnToggleStatus;

    @FXML
    private Button btnClose;

    private Product product;
    private Runnable onUpdateCallback;

    /**
     * Nạp dữ liệu sản phẩm vào Modal và cấu hình giao diện.
     *
     * @param product Sản phẩm cần hiển thị
     * @param onUpdateCallback Callback thông báo cho MyProductsController khi thông tin thay đổi
     */
    public void setProduct(Product product, Runnable onUpdateCallback) {
        this.product = product;
        this.onUpdateCallback = onUpdateCallback;

        if (product == null) {
            return;
        }

        // 1. Cập nhật thông tin tiêu đề và Badge
        lblProductIdBadge.setText("Mã SP: " + product.getId());
        lblProductName.setText(product.getName());
        lblProductPrice.setText(product.getFormattedPrice());
        lblStockQuantity.setText(product.getStock() + " sản phẩm");
        lblSoldQuantity.setText(product.getFormattedSoldQuantity() + " lượt");
        lblRating.setText(product.getFormattedRating());

        String desc = product.getDescription();
        if (desc == null || desc.trim().isEmpty()) {
            lblDescription.setText("Chưa có mô tả chi tiết cho sản phẩm này.");
        } else {
            lblDescription.setText(desc);
        }

        // 2. Nạp ảnh đại diện chính
        loadMainImage(product.getImageUrl());

        // 3. Render bộ sưu tập ảnh chi tiết (Gallery)
        renderGallery(product.getDetailImagePaths());

        // 4. Cập nhật trạng thái kinh doanh và Nút Toggle
        updateStatusUI();
    }

    /**
     * Nạp ảnh đại diện vào ImageView chính với xử lý đường dẫn tuyệt đối hoặc resource.
     */
    private void loadMainImage(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            loadDefaultImage();
            return;
        }

        try {
            Image image;
            if (imageUrl.startsWith("file:") || imageUrl.startsWith("http:") || imageUrl.startsWith("https:")) {
                image = new Image(imageUrl, 220, 220, true, true);
            } else {
                InputStream is = getClass().getResourceAsStream(imageUrl);
                if (is != null) {
                    image = new Image(is, 220, 220, true, true);
                } else {
                    image = new Image(imageUrl, 220, 220, true, true);
                }
            }
            imgMain.setImage(image);
        } catch (Exception e) {
            loadDefaultImage();
        }
    }

    private void loadDefaultImage() {
        try {
            InputStream is = getClass().getResourceAsStream(DEFAULT_IMAGE_PATH);
            if (is != null) {
                imgMain.setImage(new Image(is, 220, 220, true, true));
            }
        } catch (Exception e) {
            System.err.println("[SellerProductDetailController] Không thể nạp ảnh mặc định: " + e.getMessage());
        }
    }

    /**
     * Render danh sách ảnh xem trước trong bộ sưu tập ảnh chi tiết.
     * Người bán có thể bấm vào từng thumbnail để xem ảnh to ở ImageView chính.
     */
    private void renderGallery(List<String> detailImagePaths) {
        if (galleryContainer == null) {
            return;
        }

        galleryContainer.getChildren().clear();

        if (detailImagePaths == null || detailImagePaths.isEmpty()) {
            if (lblNoDetailImages != null) {
                galleryContainer.getChildren().add(lblNoDetailImages);
            }
            return;
        }

        for (String path : detailImagePaths) {
            if (path == null || path.trim().isEmpty()) {
                continue;
            }

            VBox thumbCard = new VBox();
            thumbCard.setStyle("-fx-background-color: white; -fx-border-color: #D1D5DB; -fx-border-radius: 6; "
                    + "-fx-background-radius: 6; -fx-padding: 3; -fx-cursor: hand;");

            ImageView thumbView = new ImageView();
            try {
                Image thumbImg;
                if (path.startsWith("file:") || path.startsWith("http:") || path.startsWith("https:")) {
                    thumbImg = new Image(path, 50, 50, true, true);
                } else {
                    InputStream is = getClass().getResourceAsStream(path);
                    thumbImg = (is != null) ? new Image(is, 50, 50, true, true) : new Image(path, 50, 50, true, true);
                }
                thumbView.setImage(thumbImg);
            } catch (Exception e) {
                // Ignore image load error
            }
            thumbView.setFitWidth(50);
            thumbView.setFitHeight(50);
            thumbView.setPreserveRatio(true);

            thumbCard.getChildren().add(thumbView);

            // Sự kiện bấm ảnh thumbnail: Đổi ảnh chính
            thumbCard.setOnMouseClicked(e -> loadMainImage(path));

            galleryContainer.getChildren().add(thumbCard);
        }
    }

    /**
     * Cập nhật màu sắc Badge và nút bấm tương ứng với trạng thái kinh doanh của sản phẩm.
     */
    private void updateStatusUI() {
        if (product == null) {
            return;
        }

        boolean active = product.isActive();
        if (active) {
            lblStatusBadge.setText("Đang kinh doanh");
            lblStatusBadge.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 4 10 4 10; -fx-background-radius: 8;");

            btnToggleStatus.setText("Tắt kinh doanh");
            btnToggleStatus.setStyle("-fx-background-color: #6B7280; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 20 10 20; -fx-cursor: hand; -fx-font-size: 13px;");
        } else {
            lblStatusBadge.setText("Đã ẩn (Tắt kinh doanh)");
            lblStatusBadge.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 4 10 4 10; -fx-background-radius: 8;");

            btnToggleStatus.setText("Bật kinh doanh lại");
            btnToggleStatus.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 20 10 20; -fx-cursor: hand; -fx-font-size: 13px;");
        }
    }

    // ========================================================================================
    // THAO TÁC NGHIỆP VỤ NGƯỜI BÁN (SELLER ACTIONS)
    // ========================================================================================

    /**
     * Xử lý bấm "Chỉnh sửa sản phẩm": Hiển thị Dialog cập nhật đơn giá và số lượng tồn kho nhanh.
     */
    @FXML
    private void handleEditProduct(ActionEvent event) {
        if (product == null) {
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Chỉnh sửa thông tin bán hàng");
        dialog.setHeaderText("Cập nhật thông tin cho: " + product.getName());

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);

        ButtonType btnSaveType = new ButtonType("Lưu thay đổi", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSaveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");

        TextField priceField = new TextField(String.valueOf((long) product.getPrice()));
        TextField stockField = new TextField(String.valueOf(product.getStock()));

        grid.add(new Label("Đơn giá mới (VNĐ):"), 0, 0);
        grid.add(priceField, 1, 0);
        grid.add(new Label("Số lượng tồn kho mới:"), 0, 1);
        grid.add(stockField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == btnSaveType) {
            String priceStr = priceField.getText().trim().replace(".", "").replace(",", "");
            String stockStr = stockField.getText().trim();

            if (!priceStr.matches("\\d+(\\.\\d+)?")) {
                throw new IllegalArgumentException("Đơn giá mới không hợp lệ!");
            }
            if (!stockStr.matches("\\d+")) {
                throw new IllegalArgumentException("Số lượng tồn kho mới phải là số nguyên dương!");
            }

            double newPrice = Double.parseDouble(priceStr);
            int newStock = Integer.parseInt(stockStr);

            if (newPrice <= 0 || newStock < 0) {
                throw new IllegalArgumentException("Giá phải > 0 và số lượng tồn kho >= 0!");
            }

            product.setPrice(newPrice);
            product.setStock(newStock);

            // Cập nhật lại UI Modal
            lblProductPrice.setText(product.getFormattedPrice());
            lblStockQuantity.setText(product.getStock() + " sản phẩm");

            if (onUpdateCallback != null) {
                onUpdateCallback.run();
            }
        }
    }

    /**
     * Xử lý bấm "Ẩn/Tắt kinh doanh": Chuyển đổi trạng thái active của sản phẩm.
     */
    @FXML
    private void handleToggleStatus(ActionEvent event) {
        if (product == null) {
            return;
        }

        boolean newStatus = !product.isActive();
        product.setActive(newStatus);

        updateStatusUI();

        if (onUpdateCallback != null) {
            onUpdateCallback.run();
        }
    }

    /**
     * Xử lý bấm "Đóng": Đóng cửa sổ Pop-up Modal.
     */
    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
}
