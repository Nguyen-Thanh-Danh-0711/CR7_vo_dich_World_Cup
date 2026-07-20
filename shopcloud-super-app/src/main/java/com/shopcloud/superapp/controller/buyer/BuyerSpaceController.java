package com.shopcloud.superapp.controller.buyer;

import com.shopcloud.superapp.model.Product;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller quản lý Không gian Người Mua (Buyer Space).
 * <p>
 * Trách nhiệm theo SRP: Quản lý danh sách sản phẩm, thực hiện tìm kiếm/lọc/sắp xếp tức thì,
 * quản lý trạng thái giỏ hàng tạm thời và hiển thị modal chi tiết sản phẩm.
 * Không tự nuốt lỗi nặng — các ngoại lệ khi nạp FXML modal được ném ra để
 * {@link com.shopcloud.superapp.exception.GlobalExceptionHandler} xử lý tập trung.
 */
public class BuyerSpaceController implements Initializable {

    private static final String DEFAULT_IMAGE_PATH = "/fxml/assets/logo.png";

    // --- Các tùy chọn sắp xếp trong ComboBox ---
    private static final String SORT_PRICE_LOW_HIGH = "Giá: Thấp đến Cao";
    private static final String SORT_PRICE_HIGH_LOW = "Giá: Cao đến Thấp";
    private static final String SORT_RATING_HIGH = "Đánh giá cao nhất (Thang 10)";
    private static final String SORT_SOLD_BEST = "Bán chạy nhất";

    // --- Ánh xạ FXML controls ---
    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private Button btnSearch;

    @FXML
    private ScrollPane productScrollPane;

    @FXML
    private TilePane productGrid;

    @FXML
    private Label cartCountLabel;

    @FXML
    private HBox toastContainer;

    @FXML
    private Label toastLabel;

    /** Danh sách sản phẩm gốc (Mock Data phong phú). */
    private final ObservableList<Product> masterProductList = FXCollections.observableArrayList();

    /** Tổng số lượng sản phẩm trong giỏ hàng tạm thời. */
    private int totalCartCount = 0;

    /** Timer ẩn thông báo Toast tự động. */
    private Timeline toastTimeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Khởi tạo dữ liệu sản phẩm mẫu (thang điểm 10, lượt bán, giá cả)
        loadMockProductCatalog();

        // 2. Khởi tạo ComboBox sắp xếp
        initSortComboBox();

        // 3. Đăng ký sự kiện lắng nghe ô tìm kiếm real-time (tự động lọc khi gõ)
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilterAndSort());

        // 4. Render danh sách sản phẩm ban đầu
        applyFilterAndSort();
    }

    /**
     * Khởi tạo các tùy chọn cho ComboBox sắp xếp và gắn listener tự động lọc/sắp xếp lại tức thì.
     */
    private void initSortComboBox() {
        sortComboBox.setItems(FXCollections.observableArrayList(
                SORT_PRICE_LOW_HIGH,
                SORT_PRICE_HIGH_LOW,
                SORT_RATING_HIGH,
                SORT_SOLD_BEST
        ));
        sortComboBox.setValue(SORT_RATING_HIGH); // Mặc định sắp xếp theo đánh giá cao nhất
        sortComboBox.setOnAction(event -> applyFilterAndSort());
    }

    /**
     * Xử lý khi nhấn nút "Tìm kiếm".
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        applyFilterAndSort();
        productScrollPane.setVvalue(0); // Cuộn lên đầu
    }

    /**
     * Luồng chính xử lý Lọc theo từ khóa và Sắp xếp danh sách sản phẩm tức thì.
     */
    private void applyFilterAndSort() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        String selectedSort = sortComboBox.getValue();

        // Bước 1: Lọc theo từ khóa (Tên hoặc Mô tả)
        List<Product> filtered = masterProductList.stream()
                .filter(p -> keyword.isEmpty()
                        || p.getName().toLowerCase(Locale.ROOT).contains(keyword)
                        || (p.getDescription() != null && p.getDescription().toLowerCase(Locale.ROOT).contains(keyword)))
                .collect(Collectors.toList());

        // Bước 2: Sắp xếp danh sách dựa trên tùy chọn ComboBox
        if (selectedSort != null) {
            switch (selectedSort) {
                case SORT_PRICE_LOW_HIGH -> filtered.sort(Comparator.comparingDouble(Product::getPrice));
                case SORT_PRICE_HIGH_LOW -> filtered.sort(Comparator.comparingDouble(Product::getPrice).reversed());
                case SORT_RATING_HIGH -> filtered.sort(Comparator.comparingDouble(Product::getRating).reversed());
                case SORT_SOLD_BEST -> filtered.sort(Comparator.comparingInt(Product::getSoldQuantity).reversed());
                default -> {
                }
            }
        }

        // Bước 3: Vẽ lại lưới sản phẩm
        renderProductGrid(filtered);
    }

    /**
     * Xóa danh sách cũ và vẽ lại các card sản phẩm.
     */
    private void renderProductGrid(List<Product> products) {
        productGrid.getChildren().clear();

        if (products.isEmpty()) {
            Label emptyLabel = new Label("Không tìm thấy sản phẩm nào phù hợp với yêu cầu.");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280; -fx-padding: 20;");
            productGrid.getChildren().add(emptyLabel);
            return;
        }

        for (Product product : products) {
            productGrid.getChildren().add(createProductCard(product));
        }
    }

    /**
     * Tạo một Card sản phẩm (VBox) theo giao diện hiện đại scannable.
     */
    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(260);
        card.getStyleClass().add("product-card");

        DropShadow shadow = new DropShadow();
        shadow.setOffsetY(4);
        shadow.setRadius(12);
        shadow.setColor(Color.color(0, 0, 0, 0.06));
        card.setEffect(shadow);

        // Ảnh sản phẩm
        ImageView imageView = createProductImageView(product.getImageUrl());

        // Tên sản phẩm
        Label nameLabel = new Label(product.getName());
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        nameLabel.setPrefHeight(42);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1F2937;");

        // Đánh giá (Thang điểm 10) & Số lượng bán
        HBox metaBox = new HBox(8);
        metaBox.setAlignment(Pos.CENTER_LEFT);

        Label ratingLabel = new Label(product.getFormattedRating());
        ratingLabel.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #D97706; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 3 8 3 8; -fx-background-radius: 10;");

        Label soldLabel = new Label("Đã bán " + product.getFormattedSoldQuantity());
        soldLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");

        metaBox.getChildren().addAll(ratingLabel, soldLabel);

        // Giá cả
        Label priceLabel = new Label(product.getFormattedPrice());
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #EF4444;");
        priceLabel.setMaxWidth(Double.MAX_VALUE);

        // Thanh công cụ thao tác trên Card
        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setMaxWidth(Double.MAX_VALUE);

        Button btnAddToCart = new Button("Thêm giỏ");
        btnAddToCart.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnAddToCart, javafx.scene.layout.Priority.ALWAYS);
        btnAddToCart.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #2563EB; -fx-font-weight: bold; -fx-background-radius: 16; -fx-border-color: #BFDBFE; -fx-border-radius: 16; -fx-cursor: hand; -fx-font-size: 12px; -fx-padding: 6 10 6 10;");
        btnAddToCart.setOnAction(e -> addToCart(product, 1));

        Button btnDetails = new Button("Chi tiết");
        btnDetails.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnDetails, javafx.scene.layout.Priority.ALWAYS);
        btnDetails.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 16; -fx-cursor: hand; -fx-font-size: 12px; -fx-padding: 6 10 6 10;");
        btnDetails.setOnAction(e -> openProductDetailModal(product));

        buttonBox.getChildren().addAll(btnAddToCart, btnDetails);

        card.getChildren().addAll(imageView, nameLabel, metaBox, priceLabel, buttonBox);
        return card;
    }

    /**
     * Nạp ảnh sản phẩm an toàn với stream fallback.
     */
    private ImageView createProductImageView(String imageUrl) {
        ImageView imageView = new ImageView();
        imageView.setFitHeight(150);
        imageView.setFitWidth(230);
        imageView.setPreserveRatio(true);

        try {
            String path = (imageUrl != null && !imageUrl.isBlank()) ? imageUrl : DEFAULT_IMAGE_PATH;
            InputStream is = getClass().getResourceAsStream(path);
            if (is != null) {
                imageView.setImage(new Image(is));
            } else {
                InputStream defaultIs = getClass().getResourceAsStream(DEFAULT_IMAGE_PATH);
                if (defaultIs != null) {
                    imageView.setImage(new Image(defaultIs));
                }
            }
        } catch (Exception e) {
            System.err.println("[BuyerSpaceController] Lỗi nạp ảnh: " + e.getMessage());
        }

        return imageView;
    }

    /**
     * Thêm sản phẩm vào giỏ hàng, cập nhật badge giỏ hàng và hiển thị Toast thông báo nhẹ.
     */
    private void addToCart(Product product, int quantity) {
        totalCartCount += quantity;
        cartCountLabel.setText("🛒 Giỏ hàng: " + totalCartCount);
        showToast("✅ Đã thêm " + quantity + " x \"" + product.getName() + "\" vào giỏ hàng thành công!");
    }

    /**
     * Mở Pop-up Modal xem chi tiết sản phẩm.
     * Ngoại lệ hệ thống khi load FXML ném ra ngoài để GlobalExceptionHandler xử lý.
     */
    private void openProductDetailModal(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/buyer/ProductDetailView.fxml"));
            Parent root = loader.load();

            ProductDetailController controller = loader.getController();
            controller.setProduct(product, this::addToCart);

            Stage modalStage = new Stage();
            modalStage.setTitle("Chi tiết sản phẩm - " + product.getName());
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setScene(new Scene(root));
            modalStage.setResizable(false);
            modalStage.showAndWait();
        } catch (Exception e) {
            throw new RuntimeException("Không thể nạp giao diện Chi tiết sản phẩm (" + product.getName() + ")!", e);
        }
    }

    /**
     * Hiển thị thông báo Toast nhẹ tự động ẩn sau 3 giây.
     */
    private void showToast(String message) {
        toastLabel.setText(message);
        toastContainer.setVisible(true);
        toastContainer.setManaged(true);

        if (toastTimeline != null) {
            toastTimeline.stop();
        }

        toastTimeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
            toastContainer.setVisible(false);
            toastContainer.setManaged(false);
        }));
        toastTimeline.play();
    }

    /**
     * Dữ liệu mẫu (Mock Data) chuẩn thang điểm 10 và lượt bán.
     */
    private void loadMockProductCatalog() {
        masterProductList.addAll(List.of(
                new Product("P001", "Áo đấu Cristiano Ronaldo CR7 World Cup 2026", 890_000, 9.8, 4300, "/fxml/assets/logo.png",
                        "Áo thi đấu chính thức phiên bản giới hạn CR7 World Cup 2026. Chất liệu vải Breathe-Fit thoáng khí cao cấp, thêu logo sắc nét."),
                new Product("P002", "Giày đá bóng Nike Mercurial CR7 Superfly", 3_450_000, 9.5, 1250, "/fxml/assets/logo.png",
                        "Giày đá sân cỏ nhân tạo cao cấp mang dấu ấn CR7. Đinh TF bám sân vượt trội, đệm Air Zoom êm ái hỗ trợ bứt tốc cực tốt."),
                new Product("P003", "Bóng đá chính thức CR7 Match Ball Size 5", 650_000, 8.7, 850, "/fxml/assets/logo.png",
                        "Bóng tiêu chuẩn FIFA Quality Pro, da PU cao cấp chống nước và độ nảy chuẩn xác trong mọi điều kiện thời tiết."),
                new Product("P004", "Băng đội trưởng CR7 Leader Edition", 220_000, 9.2, 2100, "/fxml/assets/logo.png",
                        "Băng đội trưởng phong cách CR7 dán xé tiện lợi, thắt chặt đam mê và tinh thần thủ lĩnh trên sân cỏ."),
                new Product("P005", "Balo thể thao đa năng ShopCloud x CR7 Pro", 780_000, 8.4, 5600, "/fxml/assets/logo.png",
                        "Balo tích hợp ngăn để giày riêng biệt và ngăn laptop chống sốc. Vải Polyester kháng nước 100%."),
                new Product("P006", "Đồng hồ thể thao thông minh CR7 Edition", 2_890_000, 9.9, 340, "/fxml/assets/logo.png",
                        "Đồng hồ đo nhịp tim, nhịp thở, chỉ số VO2 Max và theo dõi chế độ tập luyện thể thao chuyên nghiệp."),
                new Product("P007", "Mũ lưỡi trai CR7 Legend Snapback", 320_000, 7.9, 980, "/fxml/assets/logo.png",
                        "Mũ lưỡi trai thời trang cao cấp, thêu chữ ký CR7 dạ quang nổi bật."),
                new Product("P008", "Vớ thể thao chống trượt CR7 Match Socks", 150_000, 8.9, 3100, "/fxml/assets/logo.png",
                        "Vớ thể thao công nghệ đệm cao su dính bám chống trượt chân trong giày khi tranh chấp bóng tốc độ cao.")
        ));
    }
}
