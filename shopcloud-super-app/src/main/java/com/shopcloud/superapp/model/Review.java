package com.shopcloud.superapp.model;

/**
 * Model đại diện cho Đánh giá (Review) của Người mua đối với Sản phẩm.
 * <p>
 * Trách nhiệm theo SRP: Lưu trữ dữ liệu đánh giá bao gồm điểm thang 10,
 * nội dung nhận xét, thời gian gửi, và phản hồi từ Chủ shop (nếu có).
 */
public class Review {

    /** Mã đánh giá duy nhất (VD: RV001, RV002) */
    private String reviewId;

    /** Mã sản phẩm được đánh giá */
    private String productId;

    /** Mã cửa hàng sở hữu sản phẩm — dùng cho lọc đánh giá theo shop */
    private String shopId;

    /** Tên người dùng gửi đánh giá */
    private String username;

    /** Điểm đánh giá trên thang 10 (VD: 8.5, 9.0) */
    private double score;

    /** Nội dung nhận xét bằng text */
    private String comment;

    /** Thời gian gửi đánh giá (dạng chuỗi hiển thị) */
    private String createdAt;

    /** Phản hồi từ Chủ shop — null nếu chưa có phản hồi */
    private String sellerReply;

    public Review() {
    }

    /**
     * Constructor đầy đủ tạo đánh giá mới (chưa có phản hồi từ shop).
     */
    public Review(String reviewId, String productId, String shopId,
                  String username, double score, String comment, String createdAt) {
        this.reviewId = reviewId;
        this.productId = productId;
        this.shopId = shopId;
        this.username = username;
        this.score = score;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    /**
     * Constructor đầy đủ bao gồm phản hồi từ shop (dùng cho mock data).
     */
    public Review(String reviewId, String productId, String shopId,
                  String username, double score, String comment,
                  String createdAt, String sellerReply) {
        this(reviewId, productId, shopId, username, score, comment, createdAt);
        this.sellerReply = sellerReply;
    }

    // ========================================================================================
    // GETTERS & SETTERS
    // ========================================================================================

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getSellerReply() {
        return sellerReply;
    }

    public void setSellerReply(String sellerReply) {
        this.sellerReply = sellerReply;
    }

    // ========================================================================================
    // HELPER HIỂN THỊ
    // ========================================================================================

    /**
     * Định dạng điểm đánh giá dạng "8.5/10" để hiển thị trên giao diện.
     */
    public String getFormattedScore() {
        return String.format("%.1f/10", score);
    }

    /**
     * Kiểm tra đánh giá đã có phản hồi từ Chủ shop hay chưa.
     */
    public boolean hasSellerReply() {
        return sellerReply != null && !sellerReply.trim().isEmpty();
    }
}
