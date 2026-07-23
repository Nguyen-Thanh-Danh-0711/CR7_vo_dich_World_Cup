package com.shopcloud.superapp.model;

/**
 * Model đại diện cho Cửa hàng / Shop hoạt động trong phân hệ Quản Trị Admin.
 * <p>
 * SRP: Lưu trữ thuộc tính thông tin Shop (Mã Shop, Tên Shop, Chủ sở hữu, Ngày tham gia, Số SP, Trạng thái).
 */
public class Shop {

    private String id;
    private String name;
    private String ownerName;
    private String joinedDate;
    private int totalProducts;
    private String status; // "ACTIVE", "BANNED"

    public Shop() {
    }

    public Shop(String id, String name, String ownerName, String joinedDate, int totalProducts, String status) {
        this.id = id;
        this.name = name;
        this.ownerName = ownerName;
        this.joinedDate = joinedDate;
        this.totalProducts = totalProducts;
        this.status = status;
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

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(String joinedDate) {
        this.joinedDate = joinedDate;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isBanned() {
        return "BANNED".equalsIgnoreCase(status);
    }

    /**
     * Trả về chuỗi định dạng hiển thị trạng thái Shop.
     */
    public String getStatusText() {
        if ("BANNED".equalsIgnoreCase(status)) {
            return "Đã bị khóa (BANNED)";
        }
        return "Hoạt động";
    }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}
