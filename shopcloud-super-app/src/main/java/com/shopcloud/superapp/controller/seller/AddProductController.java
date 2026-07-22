package com.shopcloud.superapp.controller.seller;

import com.shopcloud.superapp.model.Product;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller cho màn hình Đăng bán sản phẩm mới (AddProductView).
 * <p>
 * Trách nhiệm theo SRP: Quản lý form nhập liệu, validate dữ liệu đầu vào,
 * xử lý upload ảnh đại diện và bộ sưu tập ảnh chi tiết qua FileChooser, 
 * và đăng sản phẩm mới vào {@link SellerProductStore}.
 * <p>
 * Hỗ trợ giao diện Responsive 100% diện tích workspace và quản lý tập ảnh chi tiết.
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
    private Button btnUploadGallery;

    @FXML
    private HBox galleryPreviewContainer;

    @FXML
    private Label lblGalleryCount;

    @FXML
    private Label lblGalleryPlaceholder;

    @FXML
    private Button btnPublish;

    /** Đường dẫn file ảnh đại diện đã chọn từ máy tính (null nếu chưa chọn). */
    private File selectedImageFile;

    /** Danh sách file ảnh mô tả chi tiết đã chọn. */
    private final List<File> galleryImageFiles = new ArrayList<>();

    /** Kho dữ liệu chung chia sẻ giữa AddProduct và MyProducts. */
    private final SellerProductStore productStore = SellerProductStore.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Hiển thị ảnh placeholder mặc định khi khởi tạo
        loadDefaultPreviewImage();
        renderGalleryPreviews();
    }

    // ========================================================================================
    // XỬ LÝ TẢI ẢNH ĐẠI DIỆN LÊN TỪ MÁY TÍNH (MAIN IMAGE UPLOAD)
    // ========================================================================================

    /**
     * Mở FileChooser cho phép người dùng chọn ảnh đại diện sản phẩm từ hệ thống tệp.
     * Cấu hình Window owner chính xác và ExtensionFilter đầy đủ.
     */
    @FXML
    private void handleUploadImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh đại diện sản phẩm");

        // Cấu hình bộ lọc hỗ trợ PNG, JPG, JPEG, WEBP
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Hình ảnh (PNG, JPG, JPEG, WEBP)", "*.png", "*.jpg", "*.jpeg", "*.webp"),
                new FileChooser.ExtensionFilter("Tất cả tệp (*.*)", "*.*")
        );

        // Đặt InitialDirectory an toàn về thư mục người dùng
        File userHome = new File(System.getProperty("user.home"));
        if (userHome.exists() && userHome.isDirectory()) {
            fileChooser.setInitialDirectory(userHome);
        }

        // Lấy Stage hiện tại làm Window Owner
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
    // XỬ LÝ TẢI NHIỀU ẢNH CHI TIẾT & MINH HỌA (GALLERY UPLOAD)
    // ========================================================================================

    /**
     * Mở FileChooser hỗ trợ chọn đồng thời nhiều hình ảnh minh họa chi tiết.
     */
    @FXML
    private void handleUploadGallery(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn tập ảnh chi tiết & minh họa sản phẩm");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Hình ảnh (PNG, JPG, JPEG, WEBP)", "*.png", "*.jpg", "*.jpeg", "*.webp"),
                new FileChooser.ExtensionFilter("Tất cả tệp (*.*)", "*.*")
        );

        File userHome = new File(System.getProperty("user.home"));
        if (userHome.exists() && userHome.isDirectory()) {
            fileChooser.setInitialDirectory(userHome);
        }

        Stage ownerStage = (Stage) btnUploadGallery.getScene().getWindow();
        List<File> files = fileChooser.showOpenMultipleDialog(ownerStage);

        if (files != null && !files.isEmpty()) {
            // Thêm các file mới vào danh sách hiện có (tránh trùng đường dẫn tuyệt đối)
            for (File file : files) {
                if (file != null && file.exists() && !containsFile(galleryImageFiles, file)) {
                    galleryImageFiles.add(file);
                }
            }
            renderGalleryPreviews();
        }
    }

    /**
     * Render danh sách ảnh xem trước dưới dạng HBox ngang kèm nút Xóa (icon '✕').
     */
    private void renderGalleryPreviews() {
        if (galleryPreviewContainer == null) {
            return;
        }

        galleryPreviewContainer.getChildren().clear();

        if (galleryImageFiles.isEmpty()) {
            if (lblGalleryPlaceholder != null) {
                galleryPreviewContainer.getChildren().add(lblGalleryPlaceholder);
            }
            if (lblGalleryCount != null) {
                lblGalleryCount.setText("(0 ảnh đã chọn)");
            }
            return;
        }

        if (lblGalleryCount != null) {
            lblGalleryCount.setText("(" + galleryImageFiles.size() + " ảnh đã chọn)");
        }

        for (File file : galleryImageFiles) {
            StackPane card = new StackPane();
            card.setPrefSize(96, 96);
            card.setStyle("-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 4;");

            ImageView iv = new ImageView();
            try {
                Image img = new Image(file.toURI().toString(), 88, 88, true, true);
                iv.setImage(img);
            } catch (Exception e) {
                // Fallback nếu ảnh không nạp được
            }
            iv.setFitWidth(88);
            iv.setFitHeight(88);
            iv.setPreserveRatio(true);

            // Nút xóa ảnh góc trên bên phải
            Button btnDelete = new Button("✕");
            btnDelete.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-weight: bold; "
                    + "-fx-font-size: 10px; -fx-background-radius: 12; -fx-min-width: 22px; -fx-min-height: 22px; "
                    + "-fx-max-width: 22px; -fx-max-height: 22px; -fx-padding: 0; -fx-cursor: hand;");

            btnDelete.setOnAction(e -> {
                galleryImageFiles.remove(file);
                renderGalleryPreviews();
            });

            StackPane.setAlignment(iv, Pos.CENTER);
            StackPane.setAlignment(btnDelete, Pos.TOP_RIGHT);
            StackPane.setMargin(btnDelete, new Insets(-4, -4, 0, 0));

            card.getChildren().addAll(iv, btnDelete);
            galleryPreviewContainer.getChildren().add(card);
        }
    }

    private boolean containsFile(List<File> list, File file) {
        String targetPath = file.getAbsolutePath();
        for (File f : list) {
            if (f.getAbsolutePath().equals(targetPath)) {
                return true;
            }
        }
        return false;
    }

    // ========================================================================================
    // XỬ LÝ ĐĂNG BÁN SẢN PHẨM MỚI (PUBLISH PRODUCT)
    // ========================================================================================

    /**
     * Xử lý sự kiện bấm nút "Đăng bán" — validate form, tạo Product mới, lưu ảnh gallery, thêm vào Store.
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

        // 4. Xử lý ảnh đại diện
        String mainImagePath = processUploadedImage();

        // 5. Xử lý bộ sưu tập ảnh chi tiết
        List<String> detailImagePaths = processUploadedGalleryImages();

        // 6. Tạo sản phẩm mới với mã tự sinh từ Store
        String productId = productStore.generateNextProductId();
        Product newProduct = new Product(productId, name, price, 0.0, 0, mainImagePath, description, stock);
        newProduct.setDetailImagePaths(detailImagePaths);

        // 7. Thêm vào kho dữ liệu chung
        productStore.addProduct(newProduct);

        // 8. Reset form về trạng thái ban đầu
        clearPublishForm();
    }

    // ========================================================================================
    // VALIDATION — Kiểm tra dữ liệu đầu vào
    // ========================================================================================

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

    private double parsePrice(String priceText) {
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

    private String processUploadedImage() {
        if (selectedImageFile == null || !selectedImageFile.exists()) {
            return DEFAULT_IMAGE_PATH;
        }

        try {
            Path targetDir = resolveProductAssetsDir();
            Files.createDirectories(targetDir);

            String uniqueName = System.currentTimeMillis() + "_" + selectedImageFile.getName();
            Path targetPath = targetDir.resolve(uniqueName);

            Files.copy(selectedImageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return targetPath.toUri().toString();
        } catch (IOException e) {
            System.err.println("[AddProductController] Không thể copy ảnh đại diện: " + e.getMessage());
            return selectedImageFile.toURI().toString();
        }
    }

    private List<String> processUploadedGalleryImages() {
        List<String> paths = new ArrayList<>();
        if (galleryImageFiles.isEmpty()) {
            return paths;
        }

        try {
            Path targetDir = resolveProductAssetsDir();
            Files.createDirectories(targetDir);

            for (File file : galleryImageFiles) {
                if (file != null && file.exists()) {
                    String uniqueName = System.currentTimeMillis() + "_detail_" + file.getName();
                    Path targetPath = targetDir.resolve(uniqueName);
                    Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                    paths.add(targetPath.toUri().toString());
                }
            }
        } catch (IOException e) {
            System.err.println("[AddProductController] Lỗi copy tập ảnh gallery: " + e.getMessage());
            for (File file : galleryImageFiles) {
                if (file != null && file.exists()) {
                    paths.add(file.toURI().toString());
                }
            }
        }
        return paths;
    }

    private Path resolveProductAssetsDir() {
        URL resourceUrl = getClass().getResource("/fxml/assets/");
        if (resourceUrl != null) {
            try {
                Path assetsPath = Path.of(resourceUrl.toURI());
                return assetsPath.resolve("products");
            } catch (Exception e) {
                // Fallback
            }
        }
        return Path.of("src/main/resources/fxml/assets/products");
    }

    // ========================================================================================
    // UI HELPERS
    // ========================================================================================

    private void loadDefaultPreviewImage() {
        try {
            InputStream is = getClass().getResourceAsStream(DEFAULT_IMAGE_PATH);
            if (is != null) {
                imgPreview.setImage(new Image(is, 130, 130, true, true));
                lblImagePlaceholder.setVisible(false);
                lblImagePlaceholder.setManaged(false);
            }
        } catch (Exception e) {
            System.err.println("[AddProductController] Không thể nạp ảnh mặc định: " + e.getMessage());
        }
    }

    private void clearPublishForm() {
        productNameField.clear();
        productPriceField.clear();
        productStockField.clear();
        productDescriptionArea.clear();

        selectedImageFile = null;
        loadDefaultPreviewImage();
        lblImagePath.setText("Hỗ trợ: PNG, JPG, JPEG, WEBP");
        lblImagePath.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");

        galleryImageFiles.clear();
        renderGalleryPreviews();

        productNameField.requestFocus();
    }
}
