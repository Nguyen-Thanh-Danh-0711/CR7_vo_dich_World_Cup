package com.shopcloud.superapp.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controller for the Seller Workspace (SellerSpace).
 * <p>
 * SRP: Manage product listing form, validate input, and display shop inventory in TableView.
 * No local Alert or try-catch — all exceptions propagate to
 * {@link com.shopcloud.superapp.exception.GlobalExceptionHandler}.
 */
public class SellerSpaceController implements Initializable {

    /** Minimal mock product structure for the seller inventory table. */
    private record ProductItem(String id, String name, double price, int stock) {}

    private static final int DEFAULT_NEW_PRODUCT_STOCK = 50;

    // --- @FXML bindings: must match every fx:id in SellerSpace.fxml ---

    @FXML
    private TextField productNameField;

    @FXML
    private TextField productPriceField;

    @FXML
    private TextArea productDescriptionArea;

    @FXML
    private Button btnPublish;

    @FXML
    private TableView<ProductItem> productTable;

    @FXML
    private TableColumn<ProductItem, String> colProductId;

    @FXML
    private TableColumn<ProductItem, String> colProductName;

    @FXML
    private TableColumn<ProductItem, String> colProductPrice;

    /** Backing list bound to {@link #productTable}. */
    private final ObservableList<ProductItem> productItems = FXCollections.observableArrayList();

    /** Auto-increment counter for generating new product IDs (SP001, SP002, ...). */
    private final AtomicInteger idSequence = new AtomicInteger(1);

    /** Vietnamese currency formatter for price column display. */
    private final NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Cấu hình cột TableView trước khi nạp dữ liệu
        configureTableColumns();
        bindTableDataSource();

        // Render sẵn danh sách sản phẩm mẫu khi khởi chạy
        loadMockProducts();
    }

    /**
     * Handles the "Đăng bán" button click — reads form fields, validates, and adds a new row.
     * Declares {@code throws Exception} so GlobalExceptionHandler handles all failures centrally.
     */
    @FXML
    private void handlePublishProduct(ActionEvent event) throws Exception {
        String name = productNameField.getText().trim();
        String priceText = productPriceField.getText().trim();

        validatePublishForm(name, priceText);

        double price = parsePrice(priceText);
        ProductItem newProduct = buildNewProduct(name, price);

        appendProductToTable(newProduct);
        clearPublishForm();
    }

    // ─────────────────────── Business logic (separated from UI wiring) ───────────────────────

    /**
     * Validates required TextField inputs before creating a product.
     * Ném IllegalArgumentException khi dữ liệu không hợp lệ — GlobalExceptionHandler sẽ hiển thị Alert.
     */
    private void validatePublishForm(String name, String priceText) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống!");
        }
        if (priceText.isEmpty()) {
            throw new IllegalArgumentException("Đơn giá không được để trống!");
        }
    }

    /**
     * Parses price from user input and rejects invalid or negative values.
     */
    private double parsePrice(String priceText) {
        // Loại bỏ dấu phân cách hàng nghìn (vd: "1.000.000" → "1000000")
        String normalized = priceText.replace(".", "").replace(",", "");

        if (!normalized.matches("\\d+(\\.\\d+)?")) {
            throw new IllegalArgumentException("Đơn giá phải là số hợp lệ!");
        }

        double price = Double.parseDouble(normalized);

        if (price < 0) {
            throw new IllegalArgumentException("Đơn giá không được nhỏ hơn 0!");
        }

        return price;
    }

    /**
     * Builds a new {@link ProductItem} with an auto-generated ID and default stock.
     */
    private ProductItem buildNewProduct(String name, double price) {
        String newId = generateNextProductId();
        return new ProductItem(newId, name, price, DEFAULT_NEW_PRODUCT_STOCK);
    }

    /**
     * Generates sequential product codes: SP001, SP002, ...
     */
    private String generateNextProductId() {
        return String.format("SP%03d", idSequence.getAndIncrement());
    }

    // ─────────────────────── UI helpers (small private methods) ───────────────────────

    /**
     * Maps each TableColumn to the corresponding field in {@link ProductItem}.
     * Record không có getter JavaBean nên dùng lambda thay cho PropertyValueFactory.
     */
    private void configureTableColumns() {
        colProductId.setCellValueFactory(row ->
                new SimpleStringProperty(row.getValue().id()));

        colProductName.setCellValueFactory(row ->
                new SimpleStringProperty(row.getValue().name()));

        // Hiển thị giá đã định dạng VNĐ trong cột "Đơn giá"
        colProductPrice.setCellValueFactory(row ->
                new SimpleStringProperty(formatPrice(row.getValue().price())));
    }

    /** Gắn ObservableList làm nguồn dữ liệu cho TableView. */
    private void bindTableDataSource() {
        productTable.setItems(productItems);
    }

    /** Thêm sản phẩm mới vào cuối bảng và tự động cuộn tới dòng vừa thêm. */
    private void appendProductToTable(ProductItem product) {
        productItems.add(product);
        productTable.scrollTo(product);
        productTable.getSelectionModel().select(product);
    }

    /** Xóa nội dung form sau khi đăng bán thành công. */
    private void clearPublishForm() {
        productNameField.clear();
        productPriceField.clear();
        productDescriptionArea.clear();
        productNameField.requestFocus();
    }

    /** Định dạng số tiền theo chuẩn VNĐ. */
    private String formatPrice(double price) {
        return currencyFormat.format(price) + "đ";
    }

    /**
     * Nạp danh sách sản phẩm mẫu vào TableView khi màn hình khởi tạo.
     * Mô phỏng response GET /api/products/seller/shop/{shopId}.
     */
    private void loadMockProducts() {
        List<ProductItem> mockCatalog = List.of(
                new ProductItem("SP001", "Áo đấu CR7 Home 2026", 890_000, 120),
                new ProductItem("SP002", "Giày Nike Mercurial CR7", 3_450_000, 45),
                new ProductItem("SP003", "Bóng đá CR7 Signature", 650_000, 200)
        );

        productItems.setAll(mockCatalog);
        // Đồng bộ bộ đếm ID để sản phẩm mới không trùng mã mock
        idSequence.set(mockCatalog.size() + 1);
    }
}
