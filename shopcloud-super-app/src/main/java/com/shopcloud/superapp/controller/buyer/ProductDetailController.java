package com.shopcloud.superapp.controller.buyer;

import com.shopcloud.superapp.App;
import com.shopcloud.superapp.model.Product;
import com.shopcloud.superapp.model.Review;
import com.shopcloud.superapp.store.ReviewStore;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Controller cho Pop-up Modal xem chi tiết sản phẩm (Buyer).
 * <p>
 * Trách nhiệm theo SRP: Hiển thị chi tiết sản phẩm, quản lý danh sách đánh giá,
 * form gửi đánh giá mới, nút tố cáo sản phẩm, và điều khiển cửa sổ UNDECORATED.
 * Không xử lý ngoại lệ tại chỗ — đẩy lên GlobalExceptionHandler.
 */
public class ProductDetailController {

    private static final String DEFAULT_IMAGE_PATH = "/fxml/assets/logo.png";

    // --- FXML Controls: Header Workspace Portal ---
    @FXML
    private HBox headerBar;

    @FXML
    private Button btnMinimizeHeader;

    @FXML
    private Button btnMaximizeHeader;

    @FXML
    private Button btnCloseHeader;

    // --- FXML Controls: Thông tin sản phẩm ---
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
    private Button btnBuyNow;

    @FXML
    private Button btnClose;

    @FXML
    private Button btnReport;

    // --- FXML Controls: Đánh giá ---
    @FXML
    private VBox reviewListContainer;

    @FXML
    private ComboBox<String> scoreComboBox;

    @FXML
    private TextArea reviewCommentArea;

    // --- State ---
    private Product product;
    private BiConsumer<Product, Integer> onAddToCartCallback;
    private BiConsumer<Product, Integer> onBuyNowCallback;
    private final ReviewStore reviewStore = ReviewStore.getInstance();

    // --- Trạng thái phóng to/thu nhỏ cho Modal ---
    private double lastX, lastY, lastWidth, lastHeight;
    private boolean isMaximized = false;

    /**
     * Khởi tạo dữ liệu chi tiết sản phẩm và callback xử lý.
     */
    public void setProduct(Product product, BiConsumer<Product, Integer> onAddToCartCallback, BiConsumer<Product, Integer> onBuyNowCallback) {
        this.product = product;
        this.onAddToCartCallback = onAddToCartCallback;
        this.onBuyNowCallback = onBuyNowCallback;

        if (product == null) {
            return;
        }

        // 1. Cập nhật thông tin sản phẩm
        nameLabel.setText(product.getName());
        priceLabel.setText(product.getFormattedPrice());
        ratingLabel.setText(product.getFormattedRating());
        soldLabel.setText("Đã bán " + product.getFormattedSoldQuantity());
        descriptionLabel.setText(product.getDescription() != null ? product.getDescription() : "Chưa có thông tin mô tả.");

        loadProductImage(product.getImageUrl());

        // 2. Khởi tạo ComboBox điểm đánh giá 1-10
        initScoreComboBox();

        // 3. Render danh sách đánh giá
        renderReviewList();

        // 4. Gắn kéo thả header
        setupDraggableHeader();
    }

    // ========================================================================================
    // ẢNH SẢN PHẨM (PRODUCT IMAGE)
    // ========================================================================================

    /**
     * Nạp ảnh sản phẩm từ URL hoặc đường dẫn resource, tự động fallback về logo mặc định.
     */
    private void loadProductImage(String imageUrl) {
        try {
            String path = (imageUrl != null && !imageUrl.isBlank()) ? imageUrl : DEFAULT_IMAGE_PATH;
            InputStream imageStream = getClass().getResourceAsStream(path);
            if (imageStream != null) {
                productImageView.setImage(new Image(imageStream));
            } else {
                InputStream defaultStream = getClass().getResourceAsStream(DEFAULT_IMAGE_PATH);
                if (defaultStream != null) {
                    productImageView.setImage(new Image(defaultStream));
                }
            }
        } catch (Exception e) {
            System.err.println("[ProductDetailController] Không thể tải ảnh sản phẩm: " + e.getMessage());
        }
    }

    // ========================================================================================
    // SỐ LƯỢNG (QUANTITY)
    // ========================================================================================

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

    // ========================================================================================
    // THAO TÁC MUA HÀNG (CART & BUY)
    // ========================================================================================

    @FXML
    private void handleAddToCart(ActionEvent event) {
        if (product != null && onAddToCartCallback != null) {
            int quantity = parseQuantity();
            onAddToCartCallback.accept(product, quantity);
        }
        closeStage();
    }

    @FXML
    private void handleBuyNow(ActionEvent event) {
        if (product != null && onBuyNowCallback != null) {
            int quantity = parseQuantity();
            onBuyNowCallback.accept(product, quantity);
        }
        closeStage();
    }

    @FXML
    private void handleClose(ActionEvent event) {
        closeStage();
    }

    // ========================================================================================
    // ĐÁNH GIÁ SẢN PHẨM (PRODUCT REVIEWS)
    // ========================================================================================

    /**
     * Khởi tạo ComboBox cho phép chọn điểm đánh giá từ 1 đến 10.
     */
    private void initScoreComboBox() {
        scoreComboBox.setItems(FXCollections.observableArrayList(
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"
        ));
        scoreComboBox.setValue("8"); // Mặc định 8 điểm
    }

    /**
     * Render danh sách đánh giá của sản phẩm từ ReviewStore.
     */
    private void renderReviewList() {
        reviewListContainer.getChildren().clear();

        if (product == null) {
            return;
        }

        List<Review> reviews = reviewStore.getReviewsByProductId(product.getId());

        if (reviews.isEmpty()) {
            Label emptyLabel = new Label("Chưa có đánh giá nào cho sản phẩm này.");
            emptyLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13px; -fx-font-style: italic;");
            reviewListContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Review review : reviews) {
            VBox reviewCard = createReviewCard(review);
            reviewListContainer.getChildren().add(reviewCard);
        }
    }

    /**
     * Tạo card hiển thị một đánh giá (tên người dùng, điểm, nhận xét, thời gian, phản hồi shop).
     */
    private VBox createReviewCard(Review review) {
        VBox card = new VBox(6);
        card.setStyle("-fx-background-color: #F9FAFB; -fx-padding: 12; -fx-background-radius: 10; "
                + "-fx-border-color: #E5E7EB; -fx-border-radius: 10;");

        // Dòng 1: Tên người dùng + Điểm + Thời gian
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label lblUser = new Label(review.getUsername());
        lblUser.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1F2937;");

        Label lblScore = new Label(review.getFormattedScore());
        lblScore.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #D97706; -fx-font-weight: bold; "
                + "-fx-font-size: 11px; -fx-padding: 2 8 2 8; -fx-background-radius: 10;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblTime = new Label(review.getCreatedAt());
        lblTime.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");

        headerRow.getChildren().addAll(lblUser, lblScore, spacer, lblTime);

        // Dòng 2: Nội dung nhận xét
        Label lblComment = new Label(review.getComment());
        lblComment.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13px; -fx-line-spacing: 3;");
        lblComment.setWrapText(true);

        card.getChildren().addAll(headerRow, lblComment);

        // Dòng 3: Phản hồi từ shop (nếu có)
        if (review.hasSellerReply()) {
            VBox replyBox = new VBox(4);
            replyBox.setStyle("-fx-background-color: #ECFDF5; -fx-padding: 8 12 8 12; "
                    + "-fx-background-radius: 8; -fx-border-color: #A7F3D0; -fx-border-radius: 8;");

            Label lblReplyHeader = new Label("↩ Phản hồi từ Shop:");
            lblReplyHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #065F46;");

            Label lblReplyContent = new Label(review.getSellerReply());
            lblReplyContent.setStyle("-fx-text-fill: #047857; -fx-font-size: 12px;");
            lblReplyContent.setWrapText(true);

            replyBox.getChildren().addAll(lblReplyHeader, lblReplyContent);
            card.getChildren().add(replyBox);
        }

        return card;
    }

    /**
     * Xử lý gửi đánh giá mới — validate điểm và nội dung nhận xét.
     */
    @FXML
    private void handleSubmitReview(ActionEvent event) throws Exception {
        if (product == null) {
            return;
        }

        // 1. Validate điểm đánh giá
        String scoreText = scoreComboBox.getValue();
        if (scoreText == null || scoreText.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn điểm đánh giá từ 1 đến 10!");
        }
        double score = Double.parseDouble(scoreText);

        // 2. Validate nội dung nhận xét
        String comment = reviewCommentArea.getText() != null ? reviewCommentArea.getText().trim() : "";
        if (comment.isEmpty()) {
            throw new IllegalArgumentException("Nội dung nhận xét không được để trống!");
        }

        // 3. Tạo Review mới
        String reviewId = reviewStore.generateNextReviewId();
        String username = App.UserSession.getUsername() != null ? App.UserSession.getUsername() : "anonymous";
        String createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        Review newReview = new Review(reviewId, product.getId(), product.getShopId(),
                username, score, comment, createdAt);

        // 4. Thêm vào ReviewStore
        reviewStore.addReview(newReview);

        // 5. Refresh danh sách đánh giá
        renderReviewList();

        // 6. Reset form
        reviewCommentArea.clear();
        scoreComboBox.setValue("8");
    }

    // ========================================================================================
    // TỐ CÁO SẢN PHẨM (REPORT PRODUCT)
    // ========================================================================================

    /**
     * Mở Modal tố cáo sản phẩm.
     */
    @FXML
    private void handleReportProduct(ActionEvent event) throws Exception {
        if (product == null) {
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/buyer/ReportProductModal.fxml"));
        Parent root = loader.load();

        ReportProductController controller = loader.getController();
        controller.setProduct(product);

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

    @FXML
    private void handleMinimize(ActionEvent event) {
        Stage stage = getModalStage();
        if (stage != null) {
            stage.setIconified(true);
        }
    }

    @FXML
    private void handleMaximizeRestore(ActionEvent event) {
        Stage stage = getModalStage();
        if (stage == null) {
            return;
        }

        stage.setMaximized(!stage.isMaximized());
        isMaximized = stage.isMaximized();

        if (btnMaximizeHeader != null) {
            btnMaximizeHeader.setText(isMaximized ? "❐" : "▢");
            btnMaximizeHeader.setTooltip(new Tooltip(isMaximized ? "Khôi phục kích thước" : "Phóng to"));
        }
    }

    // ========================================================================================
    // HELPERS
    // ========================================================================================

    private int parseQuantity() {
        try {
            int qty = Integer.parseInt(quantityTextField.getText().trim());
            return Math.max(1, qty);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void closeStage() {
        Stage stage = getModalStage();
        if (stage != null) {
            stage.close();
        }
    }

    private Stage getModalStage() {
        if (btnClose != null && btnClose.getScene() != null) {
            return (Stage) btnClose.getScene().getWindow();
        }
        if (headerBar != null && headerBar.getScene() != null) {
            return (Stage) headerBar.getScene().getWindow();
        }
        return null;
    }

    /**
     * Gắn sự kiện kéo thả chuột lên Header Bar để di chuyển cửa sổ Modal.
     */
    private void setupDraggableHeader() {
        if (headerBar == null) {
            return;
        }
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
            if (stage != null && !isMaximized) {
                stage.setX(event.getScreenX() + dragOffset[0]);
                stage.setY(event.getScreenY() + dragOffset[1]);
            }
        });
    }
}
