package com.shopcloud.superapp.controller;

import com.shopcloud.superapp.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller quản lý Không gian Người Mua (BuyerSpace).
 * <p>
 * Trách nhiệm theo SRP: Hiển thị danh sách sản phẩm, tìm kiếm và xử lý thao tác "Mua ngay".
 * Không tự bắt lỗi hay hiển thị Alert — mọi ngoại lệ được ném ra để
 * {@link com.shopcloud.superapp.exception.GlobalExceptionHandler} xử lý tập trung.
 */
public class BuyerSpaceController implements Initializable {

    /** Dữ liệu sản phẩm tối giản dùng cho lớp View (mock — thay bằng API sau). */
    private record ProductItem(String id, String name, double price, String category) {}

    private static final String DEFAULT_IMAGE_PATH = "/fxml/assets/logo.png";

    // --- Ánh xạ @FXML: 100% khớp fx:id trong BuyerSpace.fxml ---

    @FXML
    private TextField searchField;

    @FXML
    private Button btnSearch;

    @FXML
    private ScrollPane productScrollPane;

    @FXML
    private TilePane productGrid;

    /** Toàn bộ sản phẩm mock (nguồn dữ liệu gốc trước khi lọc). */
    private List<ProductItem> allProducts = new ArrayList<>();

    /** Định dạng tiền tệ Việt Nam cho nhãn giá. */
    private final NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Nạp dữ liệu mock và render lưới sản phẩm lần đầu
        allProducts = buildMockProductCatalog();
        renderProductGrid(allProducts);
    }

    /**
     * Sự kiện bấm nút "Tìm kiếm" — lọc sản phẩm theo từ khóa và cập nhật lưới.
     * Khai báo throws Exception để GlobalExceptionHandler xử lý mọi lỗi phát sinh.
     */
    @FXML
    private void handleSearch(ActionEvent event) throws Exception {
        String keyword = searchField.getText().trim();

        List<ProductItem> filtered = keyword.isEmpty()
                ? allProducts
                : filterProductsByKeyword(allProducts, keyword);

        renderProductGrid(filtered);
        // Cuộn về đầu danh sách sau mỗi lần tìm kiếm
        productScrollPane.setVvalue(0);
    }

    /**
     * Xử lý đặt mua khi người dùng bấm "Mua ngay" trên thẻ sản phẩm (tạo động).
     * Ném RuntimeException/IllegalArgumentException — GlobalExceptionHandler sẽ bắt trên UI Thread.
     */
    private void purchaseProduct(ProductItem product) {
        if (product == null) {
            throw new IllegalStateException("Không xác định được sản phẩm cần mua!");
        }

        validateBuyerSession();
        processPurchase(product);
    }

    // ─────────────────────── Logic nghiệp vụ (tách khỏi UI) ───────────────────────

    /**
     * Kiểm tra session đăng nhập trước khi đặt hàng.
     */
    private void validateBuyerSession() {
        String username = App.UserSession.getUsername();
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("Vui lòng đăng nhập trước khi mua hàng!");
        }
    }

    /**
     * Mô phỏng đặt hàng — sau này thay bằng gọi Order Service REST API.
     */
    private void processPurchase(ProductItem product) {
        if (product.price() <= 0) {
            throw new IllegalArgumentException("Sản phẩm \"" + product.name() + "\" có giá không hợp lệ!");
        }

        // Mock: ghi log đơn hàng ra console (thay bằng HTTP POST /api/orders)
        System.out.printf(
                "[BuyerSpace] %s đã đặt mua: %s (%s) — %s%n",
                App.UserSession.getUsername(),
                product.name(),
                product.id(),
                formatPrice(product.price())
        );
    }

    /**
     * Lọc sản phẩm theo tên hoặc danh mục (không phân biệt hoa thường).
     */
    private List<ProductItem> filterProductsByKeyword(List<ProductItem> source, String keyword) {
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT);
        return source.stream()
                .filter(p -> p.name().toLowerCase(Locale.ROOT).contains(lowerKeyword)
                        || p.category().toLowerCase(Locale.ROOT).contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    // ─────────────────────── Logic cập nhật UI (tách hàm nhỏ) ───────────────────────

    /**
     * Xóa lưới cũ và vẽ lại toàn bộ thẻ sản phẩm từ danh sách đầu vào.
     */
    private void renderProductGrid(List<ProductItem> products) {
        productGrid.getChildren().clear();

        if (products.isEmpty()) {
            productGrid.getChildren().add(createEmptyStateLabel());
            return;
        }

        for (ProductItem product : products) {
            productGrid.getChildren().add(createProductCard(product));
        }
    }

    /**
     * Tạo một thẻ sản phẩm (VBox) giữ nguyên style gốc trong BuyerSpace.fxml.
     */
    private VBox createProductCard(ProductItem product) {
        VBox card = new VBox(10);
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setStyle(
                "-fx-background-color: #FFFFFF; -fx-background-radius: 12; "
                        + "-fx-border-color: #E5E7EB; -fx-border-radius: 12; "
                        + "-fx-border-width: 1; -fx-padding: 12;"
        );
        card.getStylesheets().add(
                getClass().getResource("/fxml/style.css").toExternalForm()
        );

        DropShadow shadow = new DropShadow();
        shadow.setOffsetY(5);
        shadow.setRadius(15);
        shadow.setColor(Color.color(0, 0, 0, 0.05));
        card.setEffect(shadow);

        ImageView imageView = createProductImageView();
        Label nameLabel = createProductNameLabel(product.name());
        Label priceLabel = createProductPriceLabel(product.price());
        Button buyButton = createBuyNowButton(product);

        card.getChildren().addAll(imageView, nameLabel, priceLabel, buyButton);
        return card;
    }

    /** Ảnh sản phẩm — dùng logo mặc định nếu chưa có URL riêng. */
    private ImageView createProductImageView() {
        ImageView imageView = new ImageView();
        imageView.setFitHeight(150);
        imageView.setFitWidth(180);
        imageView.setPreserveRatio(true);
        imageView.setPickOnBounds(true);

        // Nạp ảnh dạng Stream an toàn với đường dẫn mới (Cách 2)
        try {
            var imageStream = getClass().getResourceAsStream(DEFAULT_IMAGE_PATH);
            if (imageStream != null) {
                imageView.setImage(new Image(imageStream));
            } else {
                System.out.println("=== CẢNH BÁO: KHÔNG TÌM THẤY ẢNH TẠI " + DEFAULT_IMAGE_PATH + " ===");
            }
        } catch (Exception e) {
            System.out.println("=== LỖI ĐỌC ẢNH: " + e.getMessage() + " ===");
        }

        return imageView;
    }

    /** Nhãn tên sản phẩm. */
    private Label createProductNameLabel(String name) {
        Label label = new Label(name);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1f2937;");
        return label;
    }

    /** Nhãn giá bán (màu đỏ nổi bật). */
    private Label createProductPriceLabel(double price) {
        Label label = new Label(formatPrice(price));
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #ef4444;");
        return label;
    }

    /** Nút "Mua ngay" — gắn sự kiện mua hàng trực tiếp (không qua onAction FXML). */
    private Button createBuyNowButton(ProductItem product) {
        Button buyButton = new Button("Mua ngay");
        buyButton.setMaxWidth(Double.MAX_VALUE);
        buyButton.setStyle(
                "-fx-background-color: #2563EB; -fx-text-fill: white; "
                        + "-fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand;"
        );
        buyButton.setOnAction(event -> purchaseProduct(product));
        return buyButton;
    }

    /** Nhãn thông báo khi không tìm thấy sản phẩm phù hợp. */
    private Label createEmptyStateLabel() {
        Label emptyLabel = new Label("Không tìm thấy sản phẩm phù hợp.");
        emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280;");
        return emptyLabel;
    }

    /** Định dạng số tiền theo chuẩn VNĐ. */
    private String formatPrice(double price) {
        return currencyFormat.format(price) + "đ";
    }

    // ─────────────────────── Mock catalog (thay bằng Product Service) ───────────────────────

    /**
     * Dữ liệu mẫu mô phỏng response GET /api/products/buyer/list.
     */
    private List<ProductItem> buildMockProductCatalog() {
        return List.of(
                new ProductItem("P001", "Áo đấu CR7 World Cup 2026", 890_000, "Thời trang"),
                new ProductItem("P002", "Giày Nike Mercurial CR7 Edition", 3_450_000, "Giày dép"),
                new ProductItem("P003", "Bóng đá CR7 Signature Size 5", 650_000, "Thể thao"),
                new ProductItem("P004", "Mũ lưỡi trai CR7 Fan Edition", 320_000, "Phụ kiện"),
                new ProductItem("P005", "Túi đeo chéo ShopCloud x CR7", 780_000, "Phụ kiện"),
                new ProductItem("P006", "Vớ thi đấu CR7 Pro Match", 180_000, "Thể thao")
        );
    }
}
