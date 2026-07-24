package com.shopcloud.superapp.store;

import com.shopcloud.superapp.model.CartItem;
import com.shopcloud.superapp.model.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Kho dữ liệu Giỏ hàng toàn cục (Singleton) quản lý danh sách sản phẩm đã thêm vào giỏ.
 * <p>
 * Trách nhiệm theo SRP: Chỉ quản lý CRUD giỏ hàng và tính toán tổng tiền.
 * Không chứa logic UI, validate form, hay hiển thị Alert.
 */
public final class CartStore {

    /** Instance duy nhất — Eager Initialization đảm bảo thread-safe. */
    private static final CartStore INSTANCE = new CartStore();

    /** Danh sách mục giỏ hàng — bind trực tiếp vào TableView tại CartController. */
    private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();

    /** Constructor private — ngăn khởi tạo bên ngoài. */
    private CartStore() {
    }

    /** Trả về instance duy nhất của CartStore. */
    public static CartStore getInstance() {
        return INSTANCE;
    }

    /** Trả về ObservableList dùng chung cho binding UI. */
    public ObservableList<CartItem> getCartItems() {
        return cartItems;
    }

    // ========================================================================================
    // THAO TÁC GIỎ HÀNG (CART OPERATIONS)
    // ========================================================================================

    /**
     * Thêm sản phẩm vào giỏ hàng.
     * Nếu sản phẩm đã tồn tại (cùng ID) → tăng số lượng thêm.
     * Nếu chưa có → tạo CartItem mới và thêm vào danh sách.
     *
     * @param product  Sản phẩm cần thêm
     * @param quantity Số lượng thêm (>= 1)
     */
    public void addToCart(Product product, int quantity) {
        if (product == null || quantity <= 0) {
            return;
        }

        // Tìm kiếm sản phẩm đã có trong giỏ bằng Product ID
        for (CartItem item : cartItems) {
            if (item.getProduct() != null
                    && item.getProduct().getId() != null
                    && item.getProduct().getId().equals(product.getId())) {
                // Đã có → tăng số lượng
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }

        // Chưa có → thêm mới với trạng thái mặc định đã chọn
        cartItems.add(new CartItem(product, quantity));
    }

    /**
     * Xóa một mục khỏi giỏ hàng.
     *
     * @param item Mục cần xóa
     * @return true nếu xóa thành công
     */
    public boolean removeFromCart(CartItem item) {
        return cartItems.remove(item);
    }

    /**
     * Xóa tất cả các mục đã được tick chọn (selected = true) khỏi giỏ hàng.
     * Dùng sau khi đặt hàng thành công.
     */
    public void removeSelectedItems() {
        cartItems.removeIf(CartItem::isSelected);
    }

    /**
     * Xóa toàn bộ giỏ hàng.
     */
    public void clearCart() {
        cartItems.clear();
    }

    // ========================================================================================
    // TÍNH TOÁN TỔNG TIỀN (PRICE CALCULATION)
    // ========================================================================================

    /**
     * Tính tổng tiền của tất cả sản phẩm đã được tick chọn (selected = true).
     *
     * @return Tổng tiền các sản phẩm đã chọn
     */
    public double getSelectedTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                total += item.getTotalPrice();
            }
        }
        return total;
    }

    /**
     * Đếm số lượng sản phẩm đã được tick chọn.
     *
     * @return Số mục đã chọn
     */
    public int getSelectedCount() {
        int count = 0;
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Đếm tổng số loại sản phẩm trong giỏ hàng (không phân biệt đã chọn hay chưa).
     *
     * @return Tổng số mục trong giỏ
     */
    public int getCartItemCount() {
        return cartItems.size();
    }

    /**
     * Đặt trạng thái chọn (selected) cho tất cả mục trong giỏ.
     * Dùng cho checkbox "Chọn tất cả" / "Bỏ chọn tất cả".
     *
     * @param selected true = chọn tất cả, false = bỏ chọn tất cả
     */
    public void setAllSelected(boolean selected) {
        for (CartItem item : cartItems) {
            item.setSelected(selected);
        }
    }
}
