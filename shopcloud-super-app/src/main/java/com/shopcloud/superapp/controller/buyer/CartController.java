package com.shopcloud.superapp.controller.buyer;

import com.shopcloud.superapp.model.CartItem;
import com.shopcloud.superapp.store.CartStore;
import com.shopcloud.superapp.util.WorkspaceHeaderFactory;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Controller quản lý giao diện Giỏ hàng (CartView).
 * <p>
 * Trách nhiệm theo SRP: Hiển thị danh sách CartItem từ CartStore,
 * quản lý checkbox chọn/bỏ chọn, tính tổng tiền, và mở CheckoutModal.
 * Không xử lý ngoại lệ tại chỗ — đẩy lên GlobalExceptionHandler.
 */
public class CartController implements Initializable {

    // --- FXML Controls ---

    @FXML
    private HBox headerBar;

    @FXML
    private Button btnMinimize;

    @FXML
    private Button btnMaximize;

    @FXML
    private Button btnCloseWindow;

    @FXML
    private CheckBox cbSelectAll;

    @FXML
    private Label lblCartSummary;

    @FXML
    private VBox cartItemContainer;

    @FXML
    private Label lblEmptyCart;

    @FXML
    private Label lblTotalPrice;

    @FXML
    private Button btnCheckout;

    /** Kho dữ liệu giỏ hàng chung. */
    private final CartStore cartStore = CartStore.getInstance();

    /** Bộ định dạng tiền VNĐ. */
    private final NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

    // --- Trạng thái phóng to/thu nhỏ cho Modal ---
    private double lastX, lastY, lastWidth, lastHeight;
    private boolean isMaximized = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Render danh sách giỏ hàng ban đầu
        renderCartItems();

        // 2. Cập nhật tổng tiền
        updateTotalPrice();

        // 3. Lắng nghe thay đổi danh sách giỏ hàng
        cartStore.getCartItems().addListener((ListChangeListener<CartItem>) change -> {
            renderCartItems();
            updateTotalPrice();
        });
    }

    // ========================================================================================
    // RENDER DANH SÁCH GIỎ HÀNG (CART ITEMS RENDERING)
    // ========================================================================================

    /**
     * Render toàn bộ danh sách CartItem thành các dòng UI trong VBox.
     * Mỗi dòng gồm: CheckBox, Tên SP, Đơn giá, Bộ chọn số lượng, Thành tiền, Nút xóa.
     */
    private void renderCartItems() {
        cartItemContainer.getChildren().clear();

        if (cartStore.getCartItems().isEmpty()) {
            cartItemContainer.getChildren().add(lblEmptyCart);
            lblCartSummary.setText("0 sản phẩm trong giỏ hàng");
            return;
        }

        lblCartSummary.setText(cartStore.getCartItemCount() + " sản phẩm trong giỏ hàng");

        for (CartItem item : cartStore.getCartItems()) {
            HBox row = createCartItemRow(item);
            cartItemContainer.getChildren().add(row);
        }
    }

    /**
     * Tạo một dòng UI đại diện cho một CartItem trong giỏ hàng.
     */
    private HBox createCartItemRow(CartItem item) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 12 20 12 20; -fx-border-color: transparent transparent #F3F4F6 transparent;");

        // CheckBox chọn/bỏ chọn
        CheckBox cb = new CheckBox();
        cb.setSelected(item.isSelected());
        cb.setOnAction(e -> {
            item.setSelected(cb.isSelected());
            updateTotalPrice();
            updateSelectAllCheckbox();
        });

        // Tên sản phẩm
        Label lblName = new Label(item.getProduct().getName());
        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1F2937;");
        lblName.setWrapText(true);
        lblName.setMaxWidth(200);
        HBox.setHgrow(lblName, Priority.ALWAYS);

        // Đơn giá
        Label lblPrice = new Label(item.getProduct().getFormattedPrice());
        lblPrice.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        lblPrice.setMinWidth(90);

        // Bộ chọn số lượng (-/+)
        HBox quantityBox = createQuantitySelector(item);

        // Thành tiền
        Label lblTotal = new Label(item.getFormattedTotalPrice());
        lblTotal.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-font-size: 14px;");
        lblTotal.setMinWidth(110);

        // Nút xóa
        Button btnDelete = new Button("✕");
        btnDelete.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-font-weight: bold; "
                + "-fx-background-radius: 12; -fx-min-width: 28; -fx-min-height: 28; -fx-cursor: hand; -fx-font-size: 11px;");
        btnDelete.setOnAction(e -> {
            cartStore.removeFromCart(item);
            renderCartItems();
            updateTotalPrice();
            updateSelectAllCheckbox();
        });

        row.getChildren().addAll(cb, lblName, lblPrice, quantityBox, lblTotal, btnDelete);
        return row;
    }

    /**
     * Tạo bộ chọn số lượng (nút - / ô text / nút +) cho một CartItem.
     */
    private HBox createQuantitySelector(CartItem item) {
        HBox box = new HBox(4);
        box.setAlignment(Pos.CENTER);

        Button btnMinus = new Button("-");
        btnMinus.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #1F2937; -fx-font-weight: bold; "
                + "-fx-background-radius: 6; -fx-min-width: 28; -fx-min-height: 28; -fx-cursor: hand;");

        TextField tfQuantity = new TextField(String.valueOf(item.getQuantity()));
        tfQuantity.setPrefWidth(40);
        tfQuantity.setAlignment(Pos.CENTER);
        tfQuantity.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D1D5DB; -fx-border-radius: 6; "
                + "-fx-background-radius: 6; -fx-font-weight: bold; -fx-font-size: 12px;");

        Button btnPlus = new Button("+");
        btnPlus.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #1F2937; -fx-font-weight: bold; "
                + "-fx-background-radius: 6; -fx-min-width: 28; -fx-min-height: 28; -fx-cursor: hand;");

        btnMinus.setOnAction(e -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                tfQuantity.setText(String.valueOf(item.getQuantity()));
                renderCartItems();
                updateTotalPrice();
            }
        });

        btnPlus.setOnAction(e -> {
            item.setQuantity(item.getQuantity() + 1);
            tfQuantity.setText(String.valueOf(item.getQuantity()));
            renderCartItems();
            updateTotalPrice();
        });

        box.getChildren().addAll(btnMinus, tfQuantity, btnPlus);
        return box;
    }

    // ========================================================================================
    // CHECKBOX CHỌN TẤT CẢ (SELECT ALL)
    // ========================================================================================

    /**
     * Xử lý sự kiện checkbox "Chọn tất cả" / "Bỏ chọn tất cả".
     */
    @FXML
    private void handleSelectAll(ActionEvent event) {
        boolean selectAll = cbSelectAll.isSelected();
        cartStore.setAllSelected(selectAll);
        renderCartItems();
        updateTotalPrice();
    }

    /**
     * Cập nhật trạng thái checkbox "Chọn tất cả" dựa trên trạng thái các item riêng lẻ.
     */
    private void updateSelectAllCheckbox() {
        if (cartStore.getCartItems().isEmpty()) {
            cbSelectAll.setSelected(false);
            return;
        }
        boolean allSelected = cartStore.getCartItems().stream().allMatch(CartItem::isSelected);
        cbSelectAll.setSelected(allSelected);
    }

    // ========================================================================================
    // TÍNH TỔNG TIỀN (TOTAL PRICE CALCULATION)
    // ========================================================================================

    /**
     * Cập nhật label hiển thị tổng tiền các sản phẩm đã tick chọn.
     */
    private void updateTotalPrice() {
        double total = cartStore.getSelectedTotal();
        lblTotalPrice.setText(currencyFormat.format(total) + "đ");
    }

    // ========================================================================================
    // ĐẶT HÀNG (CHECKOUT)
    // ========================================================================================

    /**
     * Xử lý sự kiện bấm nút "Đặt hàng" — mở CheckoutModal.
     * Ném ngoại lệ nếu không có sản phẩm nào được tick chọn.
     */
    @FXML
    private void handleCheckout(ActionEvent event) throws Exception {
        // Validate: phải có ít nhất 1 sản phẩm được tick
        if (cartStore.getSelectedCount() == 0) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất 1 sản phẩm để đặt hàng!");
        }

        // Mở CheckoutModal
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/buyer/CheckoutModal.fxml"));
        Parent root = loader.load();

        CheckoutController controller = loader.getController();
        controller.setOnOrderCompleteCallback(() -> {
            renderCartItems();
            updateTotalPrice();
            updateSelectAllCheckbox();
        });

        Stage modalStage = new Stage();
        modalStage.initStyle(StageStyle.UNDECORATED);
        modalStage.setResizable(false);
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setScene(new Scene(root));
        modalStage.showAndWait();
    }

    // ========================================================================================
    // ĐIỀU KHIỂN CỬA SỔ MODAL (WINDOW CONTROLS)
    // ========================================================================================

    /**
     * Thu nhỏ cửa sổ Modal xuống Taskbar.
     */
    @FXML
    private void handleMinimize(ActionEvent event) {
        Stage stage = getModalStage();
        if (stage != null) {
            stage.setIconified(true);
        }
    }

    /**
     * Toggle phóng to / khôi phục kích thước cửa sổ Modal.
     */
    @FXML
    private void handleMaximizeRestore(ActionEvent event) {
        Stage stage = getModalStage();
        if (stage == null) {
            return;
        }

        stage.setMaximized(!stage.isMaximized());
        isMaximized = stage.isMaximized();

        if (btnMaximize != null) {
            btnMaximize.setText(isMaximized ? "❐" : "▢");
            btnMaximize.setTooltip(new Tooltip(isMaximized ? "Khôi phục kích thước" : "Phóng to"));
        }
    }

    /**
     * Đóng cửa sổ Modal giỏ hàng.
     */
    @FXML
    private void handleCloseWindow(ActionEvent event) {
        Stage stage = getModalStage();
        if (stage != null) {
            stage.close();
        }
    }

    /**
     * Lấy Stage hiện tại từ Scene Graph.
     */
    private Stage getModalStage() {
        if (headerBar != null && headerBar.getScene() != null) {
            return (Stage) headerBar.getScene().getWindow();
        }
        return null;
    }
}
