package com.shopcloud.superapp.store;

import com.shopcloud.superapp.model.Review;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Kho dữ liệu Đánh giá toàn cục (Singleton) quản lý danh sách đánh giá sản phẩm.
 * <p>
 * Trách nhiệm theo SRP: Lưu trữ, truy vấn, thêm mới đánh giá và phản hồi từ chủ shop.
 * Không chứa logic UI hay validate — các Controller tự xử lý phần đó.
 */
public final class ReviewStore {

    /** Instance duy nhất — Eager Initialization đảm bảo thread-safe. */
    private static final ReviewStore INSTANCE = new ReviewStore();

    /** Danh sách tất cả đánh giá trong hệ thống. */
    private final ObservableList<Review> reviews = FXCollections.observableArrayList();

    /** Bộ đếm tự tăng để sinh mã đánh giá duy nhất: RV001, RV002, ... */
    private final AtomicInteger idSequence = new AtomicInteger(1);

    /** Constructor private — nạp mock data khi khởi tạo. */
    private ReviewStore() {
        loadMockReviews();
    }

    /** Trả về instance duy nhất của ReviewStore. */
    public static ReviewStore getInstance() {
        return INSTANCE;
    }

    /** Trả về toàn bộ danh sách đánh giá. */
    public ObservableList<Review> getReviews() {
        return reviews;
    }

    // ========================================================================================
    // TRUY VẤN ĐÁNH GIÁ (QUERY REVIEWS)
    // ========================================================================================

    /**
     * Lọc danh sách đánh giá theo mã sản phẩm.
     *
     * @param productId Mã sản phẩm cần lọc
     * @return Danh sách đánh giá của sản phẩm đó
     */
    public List<Review> getReviewsByProductId(String productId) {
        if (productId == null) {
            return List.of();
        }
        return reviews.stream()
                .filter(r -> productId.equals(r.getProductId()))
                .collect(Collectors.toList());
    }

    /**
     * Lọc danh sách đánh giá theo mã cửa hàng (dùng cho Kênh Người Bán).
     *
     * @param shopId Mã cửa hàng cần lọc
     * @return Danh sách đánh giá các sản phẩm thuộc cửa hàng đó
     */
    public List<Review> getReviewsByShopId(String shopId) {
        if (shopId == null) {
            return List.of();
        }
        return reviews.stream()
                .filter(r -> shopId.equals(r.getShopId()))
                .collect(Collectors.toList());
    }

    // ========================================================================================
    // THÊM MỚI & CẬP NHẬT (ADD & UPDATE)
    // ========================================================================================

    /**
     * Sinh mã đánh giá tiếp theo theo định dạng RV001, RV002, ...
     *
     * @return Mã đánh giá mới duy nhất
     */
    public String generateNextReviewId() {
        return String.format("RV%03d", idSequence.getAndIncrement());
    }

    /**
     * Thêm đánh giá mới vào kho.
     *
     * @param review Đánh giá đã validate xong
     */
    public void addReview(Review review) {
        if (review != null) {
            reviews.add(review);
        }
    }

    /**
     * Thêm phản hồi từ Chủ shop cho một đánh giá cụ thể.
     *
     * @param reviewId Mã đánh giá cần phản hồi
     * @param reply    Nội dung phản hồi
     * @return true nếu tìm thấy và cập nhật thành công
     */
    public boolean addSellerReply(String reviewId, String reply) {
        if (reviewId == null || reply == null) {
            return false;
        }
        for (Review review : reviews) {
            if (reviewId.equals(review.getReviewId())) {
                review.setSellerReply(reply);
                return true;
            }
        }
        return false;
    }

    // ========================================================================================
    // DỮ LIỆU MẪU (MOCK DATA)
    // ========================================================================================

    /**
     * Nạp dữ liệu đánh giá mẫu cho các sản phẩm P001–P008 (BuyerSpace mock products).
     * Bao gồm cả đánh giá có phản hồi từ shop và chưa có phản hồi.
     */
    private void loadMockReviews() {
        List<Review> mockReviews = List.of(
                // Đánh giá sản phẩm P001 — Áo đấu CR7
                new Review("RV001", "P001", "SHOP001", "le_van_b", 9.5,
                        "Áo rất đẹp, chất vải mát và thoáng khí. In ấn sắc nét, đúng như mô tả!",
                        "2026-07-10 14:30",
                        "Cảm ơn bạn đã ủng hộ shop! Rất vui khi bạn hài lòng ❤️"),
                new Review("RV002", "P001", "SHOP001", "tran_thi_c", 8.0,
                        "Áo đẹp nhưng giao hơi chậm. Chất lượng thì OK.",
                        "2026-07-12 09:15", null),
                new Review("RV003", "P001", "SHOP001", "pham_van_d", 10.0,
                        "Tuyệt vời! Chất lượng vượt mong đợi, sẽ quay lại mua thêm.",
                        "2026-07-15 18:45",
                        "Shop cảm ơn bạn rất nhiều! Hẹn gặp lại nhé 🙏"),

                // Đánh giá sản phẩm P002 — Giày Nike Mercurial CR7
                new Review("RV004", "P002", "SHOP001", "nguyen_van_e", 9.0,
                        "Giày đá rất êm, bám sân tốt. Đóng gói cẩn thận.",
                        "2026-07-11 20:00", null),
                new Review("RV005", "P002", "SHOP001", "hoang_van_f", 7.5,
                        "Giày đẹp nhưng size hơi chật so với bảng size. Nên đặt lớn hơn 0.5 size.",
                        "2026-07-14 11:30",
                        "Cảm ơn góp ý! Shop sẽ cập nhật hướng dẫn chọn size chi tiết hơn."),

                // Đánh giá sản phẩm P003 — Bóng đá CR7
                new Review("RV006", "P003", "SHOP001", "vu_thi_g", 8.5,
                        "Bóng nảy tốt, đường may chắc chắn. Dùng được cả sân cỏ nhân tạo và sân xi măng.",
                        "2026-07-13 16:20", null),

                // Đánh giá sản phẩm P005 — Balo thể thao
                new Review("RV007", "P005", "SHOP001", "do_minh_h", 9.2,
                        "Balo rộng rãi, ngăn để giày rất tiện. Vải chống nước thật sự hiệu quả khi mưa.",
                        "2026-07-16 08:45",
                        "Cảm ơn review chất lượng! Bạn có thể liên hệ shop để nhận voucher giảm giá cho lần mua tiếp nhé!"),

                // Đánh giá sản phẩm P006 — Đồng hồ thể thao
                new Review("RV008", "P006", "SHOP001", "le_thi_k", 9.8,
                        "Đồng hồ chuẩn xác, đo nhịp tim rất nhanh. Pin dùng được 5-7 ngày. Rất đáng tiền!",
                        "2026-07-17 22:10", null)
        );

        reviews.addAll(mockReviews);
        // Đồng bộ bộ đếm ID để review mới không trùng mã mock
        idSequence.set(mockReviews.size() + 1);
    }
}
