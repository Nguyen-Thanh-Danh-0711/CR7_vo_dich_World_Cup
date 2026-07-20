package com.shopcloud.superapp.controller.buyer;

import com.shopcloud.superapp.model.Product;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.function.BiConsumer;

/**
 * Controller cho Pop-up Modal xem chi tiết sản phẩm.
 * <p>
 * Trách nhiệm theo SRP: Quản lý hiển thị toàn bộ thuộc tính chi tiết sản phẩm (Ảnh, Tên, Giá, Đánh giá, Lượt bán, Mô tả)
 * và tương tác chọn số lượng để gửi lại callback thêm vào giỏ hàng.
 */
public class ProductDetailController {

    private static final String DEFAULT_IMAGE_PATH = "/fxml/assets/logo.png";

    @FXML
    private ImageView productImageView;

    @FXML
    private Label nameLabel;

    @FXML
    private Label ratingLabel;

    @FXML
    private Label soldLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private TextField quantityTextField;

    @FXML
    private Button btnDecrease;

    @FXML
    private Button btnIncrease;

    @FXML
    private Button btnAddToCart;

    @FXML
    private Button btnClose;

    @FXML
    private Button btnCloseHeader;

    private Product product;
    private BiConsumer<Product, Integer> onAddToCartCallback;

    /**
     * Khởi tạo dữ liệu chi tiết sản phẩm và callback xử lý thêm giỏ hàng từ màn hình chính.
     *
     * @param product sản phẩm được chọn
     * @param onAddToCartCallback callback nhận (Product, int quantity) khi người dùng bấm thêm giỏ hàng
     */
    public void setProduct(Product product, BiConsumer<Product, Integer> onAddToCartCallback) {
        this.product = product;
        this.onAddToCartCallback = onAddToCartCallback;

        if (product == null) {
            return;
        }

        nameLabel.setText(product.getName());
        priceLabel.setText(product.getFormattedPrice());
        ratingLabel.setText(product.getFormattedRating());
        soldLabel.setText("🔥 Đã bán " + product.getFormattedSoldQuantity());
        descriptionLabel.setText(product.getDescription() != null ? product.getDescription() : "Chưa có thông tin mô tả.");

        loadProductImage(product.getImageUrl());
    }

    /**
     * Nạp ảnh sản phẩm từ URL hoặc đường dẫn resource, tự động fallback về logo mặc định khi xảy ra sự cố.
     */
    private void loadProductImage(String imageUrl) {
        try {
            String path = (imageUrl != null && !imageUrl.isBlank()) ? imageUrl : DEFAULT_IMAGE_PATH;
            InputStream imageStream = getClass().getResourceAsStream(path);
            if (imageStream != null) {
                productImageView.setImage(new Image(imageStream));
            } else {
                // Thử nạp logo mặc định làm phương án dự phòng
                InputStream defaultStream = getClass().getResourceAsStream(DEFAULT_IMAGE_PATH);
                if (defaultStream != null) {
                    productImageView.setImage(new Image(defaultStream));
                }
            }
        } catch (Exception e) {
            System.err.println("[ProductDetailController] Không thể tải ảnh sản phẩm: " + e.getMessage());
        }
    }

    @FXML
    private void handleIncreaseQuantity(ActionEvent event) {
        int current = parseQuantity();
        quantityTextField.setText(String.valueOf(current + 1));
    }

    @FXML
    private void handleDecreaseQuantity(ActionEvent event) {
        int current = parseQuantity();
        if (current > 1) {
            quantityTextField.setText(String.valueOf(current - 1));
        }
    }

    @FXML
    private void handleAddToCart(ActionEvent event) {
        if (product != null && onAddToCartCallback != null) {
            int quantity = parseQuantity();
            onAddToCartCallback.accept(product, quantity);
        }
        closeStage();
    }

    @FXML
    private void handleClose(ActionEvent event) {
        closeStage();
    }

    private int parseQuantity() {
        try {
            int qty = Integer.parseInt(quantityTextField.getText().trim());
            return Math.max(1, qty);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void closeStage() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
}
