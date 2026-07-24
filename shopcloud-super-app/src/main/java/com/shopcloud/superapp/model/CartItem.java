package com.shopcloud.superapp.model;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Model đại diện cho một mục trong Giỏ hàng (Shopping Cart).
 * <p>
 * Trách nhiệm theo SRP: Lưu trữ thông tin sản phẩm đã thêm vào giỏ,
 * số lượng đặt mua, trạng thái chọn (checkbox) và cung cấp tính toán thành tiền.
 */
public class CartItem {

    /** Sản phẩm tham chiếu */
    private Product product;

    /** Số lượng đặt mua */
    private int quantity;

    /** Trạng thái checkbox đã tích chọn để thanh toán hay chưa */
    private boolean selected;

    public CartItem() {
    }

    /**
     * Constructor tạo mục giỏ hàng với trạng thái mặc định: đã chọn (selected = true).
     *
     * @param product  Sản phẩm thêm vào giỏ
     * @param quantity Số lượng ban đầu (>= 1)
     */
    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = Math.max(1, quantity);
        this.selected = true; // Mặc định đã tick chọn khi thêm vào giỏ
    }

    // ========================================================================================
    // GETTERS & SETTERS
    // ========================================================================================

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(1, quantity);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    // ========================================================================================
    // TÍNH TOÁN THÀNH TIỀN
    // ========================================================================================

    /**
     * Tính tổng thành tiền = Đơn giá × Số lượng.
     *
     * @return Tổng tiền cho mục giỏ hàng này
     */
    public double getTotalPrice() {
        if (product == null) {
            return 0;
        }
        return product.getPrice() * quantity;
    }

    /**
     * Định dạng thành tiền theo chuẩn VNĐ (Ví dụ: 1.780.000đ).
     */
    public String getFormattedTotalPrice() {
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        return currencyFormat.format(getTotalPrice()) + "đ";
    }
}
