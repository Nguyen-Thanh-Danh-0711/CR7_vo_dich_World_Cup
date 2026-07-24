package com.shopcloud.superapp.controller.buyer;

import com.shopcloud.superapp.model.CartItem;
import com.shopcloud.superapp.store.CartStore;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Controller quản lý Modal xác nhận đặt hàng (Checkout Modal).
 * <p>
 * Trách nhiệm theo SRP: Validate thông tin giao hàng (SĐT, Địa chỉ),
 * hiển thị tóm tắt đơn hàng, và xử lý xác nhận thanh toán.
 * Không xử lý ngoại lệ tại chỗ — ném lên GlobalExceptionHandler.
 */
public class CheckoutController implements Initializable {

    @FXML
    private HBox headerBar;

    @FXML
    private TextField phoneField;

    @FXML
    private TextArea addressArea;

    @FXML
    private VBox orderSummaryContainer;

    @FXML
    private Label lblOrderTotal;

    @FXML
    private Button btnConfirmOrder;

    /** Kho dữ liệu giỏ hàng chung. */
    private final CartStore cartStore = CartStore.getInstance();

    /** Bộ định dạng tiền VNĐ. */
    private final NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

    /** Callback thông báo CartController khi đặt hàng xong để refresh UI. */
    private Runnable onOrderCompleteCallback;

    /** Đơn hàng mua ngay trực tiếp cho 1 sản phẩm (nếu không null). */
    private CartItem buyNowItem;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Render tóm tắt đơn hàng từ các CartItem đã tick chọn
        renderOrderSummary();

        // Gắn logic kéo thả header
        if (headerBar != null) {
            setupDraggableHeader();
        }
    }

    /**
     * Thiết lập callback gọi sau khi đặt hàng thành công.
     */
    public void setOnOrderCompleteCallback(Runnable callback) {
        this.onOrderCompleteCallback = callback;
    }

    /**
     * Cấu hình luồng "Mua ngay" trực tiếp cho duy nhất 1 sản phẩm mà không qua giỏ hàng tổng.
     */
    public void setBuyNowItem(com.shopcloud.superapp.model.Product product, int quantity) {
        if (product != null && quantity > 0) {
            this.buyNowItem = new CartItem(product, quantity);
            renderOrderSummary();
        }
    }

    // ========================================================================================
    // RENDER TÓM TẮT ĐƠN HÀNG (ORDER SUMMARY)
    // ========================================================================================

    /**
     * Hiển thị danh sách sản phẩm (từ luồng Mua ngay hoặc các item được chọn trong Giỏ hàng).
     */
    private void renderOrderSummary() {
        if (orderSummaryContainer == null) {
            return;
        }
        orderSummaryContainer.getChildren().clear();

        if (buyNowItem != null) {
            // Luồng "Mua ngay": Hiển thị duy nhất sản phẩm vừa bấm Mua ngay
            HBox row = new HBox(8);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label lblName = new Label("• " + buyNowItem.getProduct().getName());
            lblName.setStyle("-fx-text-fill: #374151; -fx-font-size: 12px;");
            lblName.setWrapText(true);
            lblName.setMaxWidth(250);
            HBox.setHgrow(lblName, javafx.scene.layout.Priority.ALWAYS);

            Label lblQty = new Label("x" + buyNowItem.getQuantity());
            lblQty.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
            lblQty.setMinWidth(30);

            Label lblPrice = new Label(buyNowItem.getFormattedTotalPrice());
            lblPrice.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-font-size: 12px;");
            lblPrice.setMinWidth(100);

            row.getChildren().addAll(lblName, lblQty, lblPrice);
            orderSummaryContainer.getChildren().add(row);

            lblOrderTotal.setText(currencyFormat.format(buyNowItem.getTotalPrice()) + "đ");
        } else {
            // Luồng Giỏ hàng tổng: Hiển thị các sản phẩm đã tick chọn
            for (CartItem item : cartStore.getCartItems()) {
                if (!item.isSelected()) {
                    continue;
                }

                HBox row = new HBox(8);
                row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                Label lblName = new Label("• " + item.getProduct().getName());
                lblName.setStyle("-fx-text-fill: #374151; -fx-font-size: 12px;");
                lblName.setWrapText(true);
                lblName.setMaxWidth(250);
                HBox.setHgrow(lblName, javafx.scene.layout.Priority.ALWAYS);

                Label lblQty = new Label("x" + item.getQuantity());
                lblQty.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
                lblQty.setMinWidth(30);

                Label lblPrice = new Label(item.getFormattedTotalPrice());
                lblPrice.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-font-size: 12px;");
                lblPrice.setMinWidth(100);

                row.getChildren().addAll(lblName, lblQty, lblPrice);
                orderSummaryContainer.getChildren().add(row);
            }

            double total = cartStore.getSelectedTotal();
            lblOrderTotal.setText(currencyFormat.format(total) + "đ");
        }
    }

    // ========================================================================================
    // XÁC NHẬN ĐẶT HÀNG (CONFIRM ORDER)
    // ========================================================================================

    /**
     * Xử lý sự kiện bấm "Xác nhận đặt hàng".
     * Validate SĐT (regex: bắt đầu bằng 0, 10-11 chữ số) và Địa chỉ (không trống).
     * Ném IllegalArgumentException nếu dữ liệu không hợp lệ.
     */
    @FXML
    private void handleConfirmOrder(ActionEvent event) throws Exception {
        String phone = phoneField.getText() != null ? phoneField.getText().trim() : "";
        String address = addressArea.getText() != null ? addressArea.getText().trim() : "";

        // 1. Validate Số điện thoại
        if (phone.isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại nhận hàng không được để trống!");
        }
        if (!phone.matches("^0[0-9]{9,10}$")) {
            throw new IllegalArgumentException(
                    "Số điện thoại không hợp lệ! Phải bắt đầu bằng số 0 và có 10-11 chữ số (VD: 0912345678).");
        }

        // 2. Validate Địa chỉ
        if (address.isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ nhận hàng không được để trống!");
        }

        // 3. Hoàn tất đơn hàng
        if (buyNowItem != null) {
            // Luồng "Mua ngay": Không cần xóa sản phẩm khỏi giỏ hàng tổng
        } else {
            // Luồng Giỏ hàng tổng: Xóa các sản phẩm đã chọn khỏi giỏ hàng
            cartStore.removeSelectedItems();
        }

        // 4. Gọi callback để refresh UI nếu có
        if (onOrderCompleteCallback != null) {
            onOrderCompleteCallback.run();
        }

        // 5. Đóng modal checkout
        closeStage();
    }

    // ========================================================================================
    // ĐIỀU KHIỂN CỬA SỔ (WINDOW CONTROLS)
    // ========================================================================================

    @FXML
    private void handleMinimize(ActionEvent event) {
        Stage stage = getModalStage();
        if (stage != null) {
            stage.setIconified(true);
        }
    }

    @FXML
    private void handleCloseWindow(ActionEvent event) {
        closeStage();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeStage();
    }

    private void closeStage() {
        Stage stage = getModalStage();
        if (stage != null) {
            stage.close();
        }
    }

    private Stage getModalStage() {
        if (headerBar != null && headerBar.getScene() != null) {
            return (Stage) headerBar.getScene().getWindow();
        }
        return null;
    }

    /**
     * Gắn sự kiện kéo thả chuột lên Header Bar để di chuyển cửa sổ Modal.
     */
    private void setupDraggableHeader() {
        final double[] dragOffset = new double[2];

        headerBar.setOnMousePressed(event -> {
            Stage stage = getModalStage();
            if (stage != null) {
                dragOffset[0] = stage.getX() - event.getScreenX();
                dragOffset[1] = stage.getY() - event.getScreenY();
            }
        });

        headerBar.setOnMouseDragged(event -> {
            Stage stage = getModalStage();
            if (stage != null) {
                stage.setX(event.getScreenX() + dragOffset[0]);
                stage.setY(event.getScreenY() + dragOffset[1]);
            }
        });
    }
}
