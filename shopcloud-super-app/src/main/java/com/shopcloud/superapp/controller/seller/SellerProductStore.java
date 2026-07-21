package com.shopcloud.superapp.controller.seller;

import com.shopcloud.superapp.model.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Kho dữ liệu chung (Singleton) chia sẻ danh sách sản phẩm của Người bán
 * giữa {@link AddProductController} và {@link MyProductsController}.
 * <p>
 * Trách nhiệm theo SRP: Chỉ quản lý danh sách sản phẩm dùng chung và bộ đếm mã SP tự động.
 * Không chứa logic UI hay validate — các Controller tự xử lý phần đó.
 */
public final class SellerProductStore {

    /** Instance duy nhất — Eager Initialization đảm bảo thread-safe. */
    private static final SellerProductStore INSTANCE = new SellerProductStore();

    /** Danh sách sản phẩm của người bán — được bind trực tiếp vào TableView tại MyProductsController. */
    private final ObservableList<Product> products = FXCollections.observableArrayList();

    /** Bộ đếm tự tăng để sinh mã sản phẩm duy nhất: SP001, SP002, ... */
    private final AtomicInteger idSequence = new AtomicInteger(1);

    /** Constructor private — ngăn khởi tạo bên ngoài. */
    private SellerProductStore() {
        loadMockProducts();
    }

    /** Trả về instance duy nhất của SellerProductStore. */
    public static SellerProductStore getInstance() {
        return INSTANCE;
    }

    /** Trả về ObservableList dùng chung — mọi thay đổi sẽ tự động phản ánh lên TableView đã bind. */
    public ObservableList<Product> getProducts() {
        return products;
    }

    /**
     * Sinh mã sản phẩm tiếp theo theo định dạng SP001, SP002, ...
     *
     * @return Mã sản phẩm mới duy nhất
     */
    public String generateNextProductId() {
        return String.format("SP%03d", idSequence.getAndIncrement());
    }

    /**
     * Thêm sản phẩm mới vào kho dữ liệu chung.
     *
     * @param product Sản phẩm cần thêm (đã validate trước khi gọi)
     */
    public void addProduct(Product product) {
        products.add(product);
    }

    /**
     * Xóa sản phẩm khỏi kho dữ liệu chung.
     *
     * @param product Sản phẩm cần xóa
     * @return true nếu xóa thành công, false nếu không tìm thấy
     */
    public boolean removeProduct(Product product) {
        return products.remove(product);
    }

    /**
     * Nạp danh sách sản phẩm mẫu (Mock Data) khi khởi tạo.
     * Mô phỏng response GET /api/products/seller/shop/{shopId}.
     */
    private void loadMockProducts() {
        List<Product> mockCatalog = List.of(
                new Product("SP001", "Áo đấu CR7 Home 2026", 890_000, 9.8, 4300,
                        "/fxml/assets/logo.png",
                        "Áo thi đấu chính thức phiên bản giới hạn CR7 World Cup 2026. Chất liệu vải Breathe-Fit thoáng khí cao cấp.",
                        120),
                new Product("SP002", "Giày Nike Mercurial CR7", 3_450_000, 9.5, 1250,
                        "/fxml/assets/logo.png",
                        "Giày đá sân cỏ nhân tạo cao cấp mang dấu ấn CR7. Đinh TF bám sân vượt trội, đệm Air Zoom êm ái.",
                        45),
                new Product("SP003", "Bóng đá CR7 Signature", 650_000, 8.7, 850,
                        "/fxml/assets/logo.png",
                        "Bóng tiêu chuẩn FIFA Quality Pro, da PU cao cấp chống nước và độ nảy chuẩn xác.",
                        200)
        );

        products.setAll(mockCatalog);
        // Đồng bộ bộ đếm ID để sản phẩm mới không trùng mã mock
        idSequence.set(mockCatalog.size() + 1);
    }
}
