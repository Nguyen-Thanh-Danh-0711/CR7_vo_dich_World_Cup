package com.shopcloud.superapp.util;

import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Factory tiện ích tạo thanh Header Bar chuẩn Workspace Portal cho tất cả Modal/Pop-up.
 * <p>
 * Trách nhiệm theo SRP: Chỉ chịu trách nhiệm sinh UI Header Bar đồng bộ thương hiệu
 * (màu nền xanh navy #1E3A8A, Logo đám mây + Tiêu đề + 3 nút điều khiển),
 * và gắn logic tương tác cửa sổ (kéo thả, phóng to/thu nhỏ, đóng) cho Modal UNDECORATED.
 * <p>
 * Không chứa logic nghiệp vụ — các Controller chỉ cần gọi static methods.
 */
public final class WorkspaceHeaderFactory {

    /** Cấm khởi tạo instance — chỉ sử dụng thông qua static methods. */
    private WorkspaceHeaderFactory() {
        throw new UnsupportedOperationException("Utility class — không được phép khởi tạo instance");
    }

    // ========================================================================================
    // TẠO HEADER BAR CHUẨN (CREATE STANDARD HEADER BAR)
    // ========================================================================================

    /**
     * Tạo HBox Header Bar chuẩn Workspace Portal với Logo + Tiêu đề + 3 nút điều khiển.
     * <p>
     * Cụm 3 nút bao gồm:
     * - Nút Thu nhỏ (`—`): Gọi stage.setIconified(true)
     * - Nút Phóng to/Khôi phục (`▢`): Toggle maximize/restore
     * - Nút Đóng (`✕` màu đỏ): Gọi stage.close()
     *
     * @param subtitle Dòng phụ đề (VD: "Chi tiết sản phẩm", "Giỏ hàng")
     * @param stage    Stage cửa sổ để gắn action handlers
     * @return HBox header bar đã cấu hình sẵn
     */
    public static HBox createHeaderBar(String subtitle, Stage stage) {
        HBox headerBar = new HBox(10);
        headerBar.setAlignment(Pos.CENTER_LEFT);
        headerBar.setPrefHeight(40);
        headerBar.setStyle("-fx-background-color: #1E3A8A; -fx-padding: 0 16 0 16;");

        // --- Logo + Thương hiệu ---
        Label brandLabel = new Label("☁ SHOP CLOUD — " + (subtitle != null ? subtitle : ""));
        brandLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        // --- Region co giãn chiếm khoảng trống ---
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // --- Cụm 3 nút điều khiển ---
        HBox controlButtons = createWindowControlButtons(stage);

        headerBar.getChildren().addAll(brandLabel, spacer, controlButtons);

        return headerBar;
    }

    /**
     * Tạo cụm 2 nút điều khiển cửa sổ Modal (Thu nhỏ và Đóng).
     * Bỏ hoàn toàn nút Phóng to theo chuẩn thiết kế Modal cố định kích thước.
     *
     * @param stage Stage cửa sổ để gắn event handler
     * @return HBox chứa 2 nút điều khiển
     */
    private static HBox createWindowControlButtons(Stage stage) {
        HBox controlBox = new HBox(8);
        controlBox.setAlignment(Pos.CENTER_RIGHT);

        // Nút Thu nhỏ xuống Taskbar
        Button btnMinimize = new Button("—");
        btnMinimize.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8; "
                + "-fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;");
        btnMinimize.setTooltip(new Tooltip("Thu nhỏ"));
        btnMinimize.setOnAction(e -> {
            if (stage != null) {
                stage.setIconified(true);
            }
        });

        // Nút Đóng cửa sổ (màu đỏ)
        Button btnClose = new Button("✕");
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #F87171; "
                + "-fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;");
        btnClose.setTooltip(new Tooltip("Đóng"));
        btnClose.setOnAction(e -> {
            if (stage != null) {
                stage.close();
            }
        });

        controlBox.getChildren().addAll(btnMinimize, btnClose);
        return controlBox;
    }

    // ========================================================================================
    // CẤU HÌNH MODAL UNDECORATED (SETUP UNDECORATED MODAL)
    // ========================================================================================

    /**
     * Cấu hình Stage Modal dạng UNDECORATED (ẩn thanh tiêu đề gốc của hệ điều hành),
     * khóa cố định kích thước (setResizable(false)) và gắn logic kéo thả chuột di chuyển cửa sổ.
     * <p>
     * Sử dụng: Gọi TRƯỚC khi stage.showAndWait().
     *
     * @param stage     Stage cần cấu hình
     * @param headerBar Thanh Header Bar sẽ dùng để kéo thả di chuyển cửa sổ
     */
    public static void setupUndecoratedModal(Stage stage, Node headerBar) {
        if (stage == null) {
            return;
        }

        // 1. Thiết lập StageStyle UNDECORATED — ẩn thanh tiêu đề mặc định của OS
        stage.initStyle(StageStyle.UNDECORATED);

        // 2. Khóa cố định kích thước thiết kế, chống kéo giãn khung viền Pop-up
        stage.setResizable(false);

        // 3. Gắn logic kéo thả chuột lên Header Bar
        if (headerBar != null) {
            makeDraggable(headerBar, stage);
        }
    }

    /**
     * Gắn sự kiện kéo thả chuột lên một Node (Header Bar) để di chuyển cửa sổ Stage.
     *
     * @param headerNode Node thanh tiêu đề bắt sự kiện kéo
     * @param stage      Cửa sổ Stage cần di chuyển
     */
    private static void makeDraggable(Node headerNode, Stage stage) {
        final double[] dragOffset = new double[2];

        // Khi nhấn chuột: Ghi nhớ khoảng cách offset
        headerNode.setOnMousePressed(event -> {
            dragOffset[0] = stage.getX() - event.getScreenX();
            dragOffset[1] = stage.getY() - event.getScreenY();
        });

        // Khi kéo giữ chuột: Di chuyển Stage theo chuột
        headerNode.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() + dragOffset[0]);
            stage.setY(event.getScreenY() + dragOffset[1]);
        });
    }
}
