package com.shopcloud.superapp.controller.seller;

import com.shopcloud.superapp.model.Review;
import com.shopcloud.superapp.store.ReviewStore;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller quản lý giao diện Đánh giá của Người bán (Seller Review Management).
 * <p>
 * Trách nhiệm theo SRP: Hiển thị danh sách đánh giá từ người mua cho các sản phẩm
 * thuộc cửa hàng hiện tại, và cung cấp chức năng gửi phản hồi cho từng đánh giá.
 * Không xử lý ngoại lệ tại chỗ — đẩy lên GlobalExceptionHandler.
 */
public class SellerReviewController implements Initializable {

    /** Mã cửa hàng mặc định (Mock Data) — sau này sẽ lấy từ App.UserSession. */
    private static final String CURRENT_SHOP_ID = "SHOP001";

    @FXML
    private Label lblTotalReviews;

    @FXML
    private VBox reviewListContainer;

    @FXML
    private Label lblEmpty;

    /** Kho dữ liệu đánh giá. */
    private final ReviewStore reviewStore = ReviewStore.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        renderReviewList();
    }

    // ========================================================================================
    // RENDER DANH SÁCH ĐÁNH GIÁ (REVIEW LIST RENDERING)
    // ========================================================================================

    /**
     * Render toàn bộ danh sách đánh giá của cửa hàng hiện tại.
     * Mỗi review hiển thị: Tên người mua, Điểm/10, Nội dung, Thời gian, Phản hồi shop.
     * Nếu chưa có phản hồi: Hiện ô TextArea + Nút "Gửi phản hồi".
     */
    private void renderReviewList() {
        reviewListContainer.getChildren().clear();

        List<Review> reviews = reviewStore.getReviewsByShopId(CURRENT_SHOP_ID);

        lblTotalReviews.setText("Tổng: " + reviews.size() + " đánh giá");

        if (reviews.isEmpty()) {
            reviewListContainer.getChildren().add(lblEmpty);
            return;
        }

        for (Review review : reviews) {
            VBox card = createReviewCard(review);
            reviewListContainer.getChildren().add(card);
        }
    }

    /**
     * Tạo card hiển thị một đánh giá bao gồm khả năng phản hồi.
     */
    private VBox createReviewCard(Review review) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 16; -fx-background-radius: 12; "
                + "-fx-border-color: #E5E7EB; -fx-border-radius: 12; -fx-border-width: 1;");

        // Dòng 1: Tên SP + Mã SP
        Label lblProductInfo = new Label("Sản phẩm: " + review.getProductId());
        lblProductInfo.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;");

        // Dòng 2: Tên người mua + Điểm + Thời gian
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label lblUser = new Label(review.getUsername());
        lblUser.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1F2937;");

        Label lblScore = new Label(review.getFormattedScore());
        lblScore.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #D97706; -fx-font-weight: bold; "
                + "-fx-font-size: 12px; -fx-padding: 3 10 3 10; -fx-background-radius: 12;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblTime = new Label(review.getCreatedAt());
        lblTime.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");

        headerRow.getChildren().addAll(lblUser, lblScore, spacer, lblTime);

        // Dòng 3: Nội dung nhận xét
        Label lblComment = new Label(review.getComment());
        lblComment.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13px; -fx-line-spacing: 3;");
        lblComment.setWrapText(true);

        card.getChildren().addAll(lblProductInfo, headerRow, lblComment);

        // Dòng 4: Phản hồi từ shop HOẶC form gửi phản hồi
        if (review.hasSellerReply()) {
            // Hiện nội dung phản hồi đã gửi
            VBox replyBox = new VBox(4);
            replyBox.setStyle("-fx-background-color: #ECFDF5; -fx-padding: 10 14 10 14; "
                    + "-fx-background-radius: 10; -fx-border-color: #A7F3D0; -fx-border-radius: 10;");

            Label lblReplyHeader = new Label("↩ Phản hồi của bạn:");
            lblReplyHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #065F46;");

            Label lblReplyContent = new Label(review.getSellerReply());
            lblReplyContent.setStyle("-fx-text-fill: #047857; -fx-font-size: 13px;");
            lblReplyContent.setWrapText(true);

            replyBox.getChildren().addAll(lblReplyHeader, lblReplyContent);
            card.getChildren().add(replyBox);
        } else {
            // Hiện form gửi phản hồi
            VBox replyForm = new VBox(8);
            replyForm.setStyle("-fx-background-color: #FFF7ED; -fx-padding: 10 14 10 14; "
                    + "-fx-background-radius: 10; -fx-border-color: #FED7AA; -fx-border-radius: 10;");

            Label lblFormTitle = new Label("Chưa có phản hồi — Gửi phản hồi ngay:");
            lblFormTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #9A3412;");

            TextArea replyArea = new TextArea();
            replyArea.setPromptText("Nhập nội dung phản hồi...");
            replyArea.setPrefRowCount(2);
            replyArea.setWrapText(true);
            replyArea.setMaxWidth(Double.MAX_VALUE);

            Button btnSendReply = new Button("Gửi phản hồi");
            btnSendReply.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold; "
                    + "-fx-background-radius: 14; -fx-padding: 6 16 6 16; -fx-cursor: hand; -fx-font-size: 12px;");

            // Gắn event handler gửi phản hồi
            btnSendReply.setOnAction(e -> {
                String replyText = replyArea.getText() != null ? replyArea.getText().trim() : "";
                if (replyText.isEmpty()) {
                    throw new IllegalArgumentException("Nội dung phản hồi không được để trống!");
                }
                reviewStore.addSellerReply(review.getReviewId(), replyText);
                renderReviewList(); // Refresh UI
            });

            replyForm.getChildren().addAll(lblFormTitle, replyArea, btnSendReply);
            card.getChildren().add(replyForm);
        }

        return card;
    }
}
