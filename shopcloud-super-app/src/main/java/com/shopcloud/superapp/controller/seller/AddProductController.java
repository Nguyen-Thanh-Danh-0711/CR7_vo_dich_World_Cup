package com.shopcloud.superapp.controller.seller;

import com.shopcloud.superapp.model.Product;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;

/**
 * Controller cho màn hình Đăng bán sản phẩm mới (AddProductView).
 * <p>
 * Trách nhiệm theo SRP: Quản lý form nhập liệu, validate dữ liệu đầu vào,
 * xử lý upload ảnh qua FileChooser, và đăng sản phẩm mới vào {@link SellerProductStore}.
 * <p>
 * Không tự bắt lỗi hay hiển thị Alert — mọi ngoại lệ (IO, NPE, IllegalArgument...)
 * được ném ra để GlobalExceptionHandler xử lý tập trung.
 */
public class AddProductController implements Initializable {

    private static final String DEFAULT_IMAGE_PATH = "/fxml/assets/logo.png";

    // --- @FXML bindings: ánh xạ với AddProductView.fxml ---

    @FXML
    private TextField productNameField;

    @FXML
    private TextField productPriceField;

    @FXML
    private TextField productStockField;

    @FXML
    private TextArea productDescriptionArea;

    @FXML
    private Button btnUploadImage;

    @FXML
    private ImageView imgPreview;

    @FXML
    private Label lblImagePlaceholder;

    @FXML
    private Label lblImagePath;

    @FXML
    private VBox imagePreviewContainer;

    @FXML
    private Button btnPublish;

    /** Đường dẫn file ảnh đã chọn từ máy tính (null nếu chưa chọn). */
    private File selectedImageFile;

    /** Kho dữ liệu chung chia sẻ giữa AddProduct và MyProducts. */
    private final SellerProductStore productStore = SellerProductStore.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Hiển thị ảnh placeholder mặc định khi khởi tạo
        loadDefaultPreviewImage();
    }

    // ========================================================================================
    // XỬ LÝ TẢI ẢNH LÊN TỪ MÁY TÍNH (IMAGE UPLOAD)
    // ========================================================================================

    /**
     * Mở FileChooser cho phép người dùng chọn ảnh sản phẩm từ hệ thống tệp.
     * <p>
     * Chỉ lọc file ảnh: *.png, *.jpg, *.jpeg.
     * Sau khi chọn, hiển thị preview trên ImageView và lưu đường dẫn file.
     */
    @FXML
    private void handleUploadImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh sản phẩm");

        // Cấu hình bộ lọc chỉ chấp nhận file ảnh
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Tệp ảnh (PNG, JPG, JPEG)", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("Tất cả các tệp", "*.*")
        );

        // Lấy Stage hiện tại để mở dialog con đúng ngữ cảnh
        Stage ownerStage = (Stage) btnUploadImage.getScene().getWindow();
        File file = fileChooser.showOpenDialog(ownerStage);

        if (file != null) {
            selectedImageFile = file;

            // Hiển thị ảnh preview trên ImageView
            Image image = new Image(file.toURI().toString(), 130, 130, true, true);
            imgPreview.setImage(image);

            // Ẩn text placeholder, hiện ảnh
            lblImagePlaceholder.setVisible(false);
            lblImagePlaceholder.setManaged(false);

            // Cập nhật label hiển thị tên file đã chọn
            lblImagePath.setText("✓ " + file.getName());
            lblImagePath.setStyle("-fx-text-fill: #059669; -fx-font-size: 11px; -fx-font-weight: bold;");
        }
    }

    // ========================================================================================
    // XỬ LÝ ĐĂNG BÁN SẢN PHẨM MỚI (PUBLISH PRODUCT)
    // ========================================================================================

    /**
     * Xử lý sự kiện bấm nút "Đăng bán" — validate form, tạo Product mới, thêm vào Store.
     * <p>
     * Ném IllegalArgumentException nếu dữ liệu không hợp lệ — GlobalExceptionHandler
     * sẽ tự động bắt và hiển thị Alert lỗi cho người dùng.
     */
    @FXML
    private void handlePublishProduct(ActionEvent event) throws Exception {
        // 1. Đọc và chuẩn hóa dữ liệu từ form
        String name = productNameField.getText().trim();
        String priceText = productPriceField.getText().trim();
        String stockText = productStockField.getText().trim();
        String description = productDescriptionArea.getText().trim();

        // 2. Validate dữ liệu bắt buộc
        validatePublishForm(name, priceText, stockText);

        // 3. Parse giá tiền và số lượng
        double price = parsePrice(priceText);
        int stock = parseStock(stockText);

        // 4. Xử lý ảnh — copy vào thư mục assets nếu đã chọn file
        String imagePath = processUploadedImage();

        // 5. Tạo sản phẩm mới với mã tự sinh từ Store
        String productId = productStore.generateNextProductId();
        Product newProduct = new Product(productId, name, price, 0.0, 0, imagePath, description, stock);

        // 6. Thêm vào kho dữ liệu chung
        productStore.addProduct(newProduct);

        // 7. Reset form về trạng thái ban đầu
        clearPublishForm();
    }

    // ========================================================================================
    // VALIDATION — Kiểm tra dữ liệu đầu vào
    // ========================================================================================

    /**
     * Validate các trường bắt buộc của form đăng bán.
     * Ném IllegalArgumentException khi dữ liệu không hợp lệ.
     */
    private void validatePublishForm(String name, String priceText, String stockText) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống!");
        }
        if (priceText.isEmpty()) {
            throw new IllegalArgumentException("Đơn giá không được để trống!");
        }
        if (stockText.isEmpty()) {
            throw new IllegalArgumentException("Số lượng tồn kho không được để trống!");
        }
    }

    /**
     * Parse giá tiền từ chuỗi nhập liệu, loại bỏ dấu phân cách hàng nghìn.
     * Ném IllegalArgumentException nếu không phải số hợp lệ hoặc giá trị âm.
     */
    private double parsePrice(String priceText) {
        // Loại bỏ dấu phân cách hàng nghìn (vd: "1.000.000" → "1000000")
        String normalized = priceText.replace(".", "").replace(",", "");

        if (!normalized.matches("\\d+(\\.\\d+)?")) {
            throw new IllegalArgumentException("Đơn giá phải là số hợp lệ! (Ví dụ: 890000 hoặc 890.000)");
        }

        double price = Double.parseDouble(normalized);

        if (price <= 0) {
            throw new IllegalArgumentException("Đơn giá phải là số dương lớn hơn 0!");
        }

        return price;
    }

    /**
     * Parse số lượng tồn kho từ chuỗi nhập liệu.
     * Ném IllegalArgumentException nếu không phải số nguyên dương.
     */
    private int parseStock(String stockText) {
        if (!stockText.matches("\\d+")) {
            throw new IllegalArgumentException("Số lượng tồn kho phải là số nguyên dương!");
        }

        int stock = Integer.parseInt(stockText);

        if (stock <= 0) {
            throw new IllegalArgumentException("Số lượng tồn kho phải lớn hơn 0!");
        }

        return stock;
    }

    // ========================================================================================
    // XỬ LÝ ẢNH SẢN PHẨM (IMAGE PROCESSING)
    // ========================================================================================

    /**
     * Xử lý ảnh đã upload — copy file vào thư mục assets/products/ của dự án.
     * Trả về đường dẫn resource nội bộ hoặc đường dẫn file tuyệt đối.
     *
     * @return Đường dẫn ảnh để lưu vào model Product
     */
    private String processUploadedImage() {
        if (selectedImageFile == null || !selectedImageFile.exists()) {
            return DEFAULT_IMAGE_PATH; // Sử dụng ảnh mặc định nếu chưa chọn
        }

        try {
            // Tạo thư mục assets/products/ nếu chưa tồn tại
            Path targetDir = resolveProductAssetsDir();
            Files.createDirectories(targetDir);

            // Tạo tên file duy nhất tránh trùng lặp: timestamp_filename
            String uniqueName = System.currentTimeMillis() + "_" + selectedImageFile.getName();
            Path targetPath = targetDir.resolve(uniqueName);

            // Copy file ảnh vào thư mục dự án
            Files.copy(selectedImageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Trả về đường dẫn tuyệt đối để nạp ảnh từ file system (không phải resource)
            return targetPath.toUri().toString();
        } catch (IOException e) {
            // Nếu không copy được, sử dụng đường dẫn file gốc trực tiếp
            System.err.println("[AddProductController] Không thể copy ảnh vào assets: " + e.getMessage());
            return selectedImageFile.toURI().toString();
        }
    }

    /**
     * Xác định đường dẫn thư mục assets/products/ tương đối với project root.
     */
    private Path resolveProductAssetsDir() {
        // Xác định vị trí thư mục resources từ classpath
        URL resourceUrl = getClass().getResource("/fxml/assets/");
        if (resourceUrl != null) {
            try {
                Path assetsPath = Path.of(resourceUrl.toURI());
                return assetsPath.resolve("products");
            } catch (Exception e) {
                // Fallback nếu không resolve được từ classpath
            }
        }
        // Fallback: tạo thư mục tương đối trong working directory
        return Path.of("src/main/resources/fxml/assets/products");
    }

    // ========================================================================================
    // UI HELPERS — Các hàm hỗ trợ giao diện
    // ========================================================================================

    /** Nạp ảnh placeholder mặc định vào ImageView khi khởi tạo. */
    private void loadDefaultPreviewImage() {
        try {
            InputStream is = getClass().getResourceAsStream(DEFAULT_IMAGE_PATH);
            if (is != null) {
                imgPreview.setImage(new Image(is, 130, 130, true, true));
                // Ẩn placeholder text khi có ảnh mặc định
                lblImagePlaceholder.setVisible(false);
                lblImagePlaceholder.setManaged(false);
            }
        } catch (Exception e) {
            System.err.println("[AddProductController] Không thể nạp ảnh mặc định: " + e.getMessage());
        }
    }

    /** Xóa toàn bộ nội dung form sau khi đăng bán thành công và reset về trạng thái ban đầu. */
    private void clearPublishForm() {
        productNameField.clear();
        productPriceField.clear();
        productStockField.clear();
        productDescriptionArea.clear();

        // Reset ảnh preview về mặc định
        selectedImageFile = null;
        loadDefaultPreviewImage();
        lblImagePath.setText("Hỗ trợ: PNG, JPG, JPEG");
        lblImagePath.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");

        // Focus lại ô tên sản phẩm
        productNameField.requestFocus();
    }
}
