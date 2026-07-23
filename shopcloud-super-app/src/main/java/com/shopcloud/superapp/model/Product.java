package com.shopcloud.superapp.model;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Model đại diện cho thông tin Sản phẩm trong Không gian Người Mua (Buyer Space).
 * <p>
 * Trách nhiệm theo SRP: Lưu trữ dữ liệu thuộc tính của sản phẩm và cung cấp các hàm hỗ trợ định dạng hiển thị.
 */
public class Product {

    private String id;
    private String name;
    private double price;
    private double rating; // Thang điểm 10 (Ví dụ: 9.5, 8.2)
    private int soldQuantity; // Số lượng đã bán
    private String imageUrl;
    private String description;
    private int stock; // Số lượng tồn kho (dùng trong Kênh Người Bán)
    private List<String> detailImagePaths = new ArrayList<>(); // Danh sách ảnh mô tả chi tiết
    private boolean active = true; // Trạng thái kinh doanh (true: Đang kinh doanh, false: Ẩn/Tắt)
    private String shopId = "SHOP001";
    private String shopName = "CR7 Official Shop";
    private String adminStatus = "APPROVED"; // "APPROVED", "REMOVED_BY_ADMIN", "PENDING"

    public Product() {
    }

    /**
     * Constructor đầy đủ cho Không gian Người Mua (BuyerSpace) — không cần stock.
     */
    public Product(String id, String name, double price, double rating, int soldQuantity, String imageUrl, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.rating = rating;
        this.soldQuantity = soldQuantity;
        this.imageUrl = imageUrl;
        this.description = description;
        this.stock = 0;
    }

    /**
     * Constructor mở rộng cho Kênh Người Bán (SellerSpace) — bao gồm stock tồn kho.
     */
    public Product(String id, String name, double price, double rating, int soldQuantity, String imageUrl, String description, int stock) {
        this(id, name, price, rating, soldQuantity, imageUrl, description);
        this.stock = stock;
    }

    /**
     * Constructor đầy đủ cho Admin Space — hỗ trợ shopId và shopName.
     */
    public Product(String id, String name, double price, double rating, int soldQuantity, String imageUrl, String description, int stock, String shopId, String shopName) {
        this(id, name, price, rating, soldQuantity, imageUrl, description, stock);
        this.shopId = shopId;
        this.shopName = shopName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getSoldQuantity() {
        return soldQuantity;
    }

    public void setSoldQuantity(int soldQuantity) {
        this.soldQuantity = soldQuantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public List<String> getDetailImagePaths() {
        if (detailImagePaths == null) {
            detailImagePaths = new ArrayList<>();
        }
        return detailImagePaths;
    }

    public void setDetailImagePaths(List<String> detailImagePaths) {
        this.detailImagePaths = detailImagePaths != null ? detailImagePaths : new ArrayList<>();
    }

    public void addDetailImagePath(String path) {
        if (path != null && !path.trim().isEmpty()) {
            if (this.detailImagePaths == null) {
                this.detailImagePaths = new ArrayList<>();
            }
            this.detailImagePaths.add(path);
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getStatusText() {
        if ("REMOVED_BY_ADMIN".equalsIgnoreCase(adminStatus)) {
            return "Đã xóa bởi Admin";
        }
        return active ? "Đang kinh doanh" : "Tạm ẩn";
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getAdminStatus() {
        return adminStatus;
    }

    public void setAdminStatus(String adminStatus) {
        this.adminStatus = adminStatus;
    }

    public String getAdminStatusText() {
        if ("REMOVED_BY_ADMIN".equalsIgnoreCase(adminStatus)) {
            return "Bị xóa vi phạm";
        } else if ("PENDING".equalsIgnoreCase(adminStatus)) {
            return "Chờ duyệt";
        }
        return "Đã duyệt";
    }

    /**
     * Định dạng giá tiền theo chuẩn VNĐ (Ví dụ: 890.000đ).
     */
    public String getFormattedPrice() {
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        return currencyFormat.format(price) + "đ";
    }

    /**
     * Định dạng lượt bán thân thiện với người dùng (Ví dụ: 1.2k hoặc 850).
     */
    public String getFormattedSoldQuantity() {
        if (soldQuantity >= 1000) {
            double kValue = soldQuantity / 1000.0;
            return String.format(Locale.US, "%.1fk", kValue).replace(".0k", "k");
        }
        return String.valueOf(soldQuantity);
    }

    /**
     * Định dạng hiển thị đánh giá thang điểm 10 (Ví dụ: 9.5/10).
     */
    public String getFormattedRating() {
        return String.format(Locale.US, "%.1f/10", rating);
    }
}
