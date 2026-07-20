package com.shopcloud.superapp.util;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * Utility Helper xử lý việc thay đổi kích thước cửa sổ bằng chuột (Window Resizing)
 * và di chuyển cửa sổ (Window Dragging) cho JavaFX Stage dạng Custom Undecorated.
 * <p>
 * Nguyên lý hoạt động:
 * 1. Bọc các Listener sự kiện chuột (MOUSE_MOVED, MOUSE_PRESSED, MOUSE_DRAGGED) trên Scene.
 * 2. Chia cạnh viền cửa sổ thành 8 vùng tương tác (4 Cạnh: Trên, Dưới, Trái, Phải và 4 Góc).
 * 3. Tự động chuyển đổi biểu tượng Cursor tương ứng và tính toán tọa độ (x, y), kích thước (width, height)
 *    để thu phóng mượt mà, đồng thời tuân thủ giới hạn Minimum Size (minWidth, minHeight).
 * <p>
 * Tuân thủ SRP: Class chỉ chịu trách nhiệm duy nhất về cơ chế resize viền cửa sổ.
 * Logic Maximize/Restore được tách riêng tại Controller.
 */
public final class WindowResizeHelper {

    /** Cấm khởi tạo instance — chỉ sử dụng thông qua các static method */
    private WindowResizeHelper() {
        throw new UnsupportedOperationException("Utility class — không được phép khởi tạo instance");
    }

    // ======================== BIẾN TRẠNG THÁI NỘI BỘ KHI KÉO VIỀN ========================
    /** Loại Cursor hiện tại đang được hiển thị (xác định hướng kéo giãn) */
    private static Cursor currentCursor = Cursor.DEFAULT;

    /** Tọa độ màn hình gốc khi bắt đầu nhấn chuột */
    private static double startMouseX, startMouseY;

    /** Vị trí và kích thước Stage gốc tại thời điểm bắt đầu nhấn chuột */
    private static double startStageX, startStageY, startStageWidth, startStageHeight;

    /**
     * Đăng ký nhanh cơ chế Resize 8 hướng cho Stage với cấu hình mặc định:
     * minWidth = 1000, minHeight = 650, biên cảm ứng = 8px.
     *
     * @param stage Stage cần kích hoạt tính năng kéo giãn viền
     * @throws IllegalArgumentException nếu stage là null
     * @throws IllegalStateException    nếu Scene chưa được gán cho Stage
     */
    public static void enableResizing(Stage stage) {
        enableResizing(stage, 1000, 650, 8);
    }

    /**
     * Đăng ký cơ chế Resize 8 hướng cho Stage với tham số tùy chỉnh.
     *
     * @param stage        Stage cần kích hoạt tính năng kéo giãn viền
     * @param minWidth     Chiều rộng tối thiểu cho phép (tránh vỡ layout)
     * @param minHeight    Chiều cao tối thiểu cho phép
     * @param borderMargin Độ rộng vùng viền cảm ứng chuột (pixel)
     * @throws IllegalArgumentException nếu stage là null
     * @throws IllegalStateException    nếu Scene chưa được gán cho Stage
     */
    public static void enableResizing(Stage stage, double minWidth, double minHeight, double borderMargin) {
        if (stage == null) {
            throw new IllegalArgumentException("Stage không được phép null");
        }
        Scene scene = stage.getScene();
        if (scene == null) {
            throw new IllegalStateException("Scene chưa được gán cho Stage. Hãy gọi setScene() trước khi enableResizing().");
        }

        // Đảm bảo giá trị hợp lệ tối thiểu
        double effectiveMinWidth = Math.max(100, minWidth);
        double effectiveMinHeight = Math.max(100, minHeight);
        double effectiveBorderMargin = Math.max(2, borderMargin);

        // Gắn 3 Listener chuột chính vào Scene
        scene.addEventHandler(MouseEvent.MOUSE_MOVED,
                event -> handleMouseMoved(event, stage, effectiveBorderMargin));
        scene.addEventHandler(MouseEvent.MOUSE_PRESSED,
                event -> handleMousePressed(event, stage));
        scene.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                event -> handleMouseDragged(event, stage, effectiveMinWidth, effectiveMinHeight));
    }

    /**
     * Cho phép kéo rê một Node (ví dụ: thanh Header / Title Bar) để di chuyển cửa sổ.
     * Hỗ trợ tùy chọn double-click để kích hoạt Phóng to / Khôi phục.
     *
     * @param headerNode   Node thanh tiêu đề dùng để bắt sự kiện kéo
     * @param stage        Cửa sổ Stage cần di chuyển
     * @param onDoubleClick Callback tùy chọn khi double click (ví dụ: Maximize/Restore), có thể null
     */
    public static void makeDraggable(Node headerNode, Stage stage, Runnable onDoubleClick) {
        if (headerNode == null || stage == null) {
            return;
        }

        // Mảng 2 phần tử lưu offset giữa vị trí chuột và góc trên-trái cửa sổ
        final double[] dragOffset = new double[2];

        // Khi nhấn chuột: Ghi nhớ khoảng cách offset từ chuột đến góc Stage
        headerNode.setOnMousePressed(event -> {
            dragOffset[0] = stage.getX() - event.getScreenX();
            dragOffset[1] = stage.getY() - event.getScreenY();
        });

        // Khi kéo giữ chuột: Di chuyển Stage theo chuột (chỉ khi không Maximized)
        headerNode.setOnMouseDragged(event -> {
            if (!stage.isMaximized()) {
                stage.setX(event.getScreenX() + dragOffset[0]);
                stage.setY(event.getScreenY() + dragOffset[1]);
            }
        });

        // Khi double-click: Kích hoạt callback Maximize/Restore (nếu có)
        if (onDoubleClick != null) {
            headerNode.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    onDoubleClick.run();
                }
            });
        }
    }

    // ======================== XỬ LÝ SỰ KIỆN CHUỘT NỘI BỘ ========================

    /**
     * Xử lý sự kiện MOUSE_MOVED: Xác định vùng viền chuột đang đứng và thay đổi Cursor.
     * Khi cửa sổ đang Maximized thì luôn trả về Cursor mặc định (không cho resize).
     */
    private static void handleMouseMoved(MouseEvent event, Stage stage, double borderMargin) {
        // Khi cửa sổ đã phóng to toàn màn hình → không hiển thị cursor resize
        if (stage.isMaximized()) {
            if (currentCursor != Cursor.DEFAULT) {
                currentCursor = Cursor.DEFAULT;
                stage.getScene().setCursor(Cursor.DEFAULT);
            }
            return;
        }

        double mouseX = event.getSceneX();
        double mouseY = event.getSceneY();
        double width = stage.getWidth();
        double height = stage.getHeight();

        // Xác định chuột đang nằm ở vùng viền nào của cửa sổ
        boolean isTop = mouseY <= borderMargin;
        boolean isBottom = mouseY >= height - borderMargin;
        boolean isLeft = mouseX <= borderMargin;
        boolean isRight = mouseX >= width - borderMargin;

        // Xác định Cursor phù hợp theo 8 hướng + trạng thái mặc định
        if (isTop && isLeft) {
            currentCursor = Cursor.NW_RESIZE;       // Góc trên-trái
        } else if (isTop && isRight) {
            currentCursor = Cursor.NE_RESIZE;       // Góc trên-phải
        } else if (isBottom && isLeft) {
            currentCursor = Cursor.SW_RESIZE;       // Góc dưới-trái
        } else if (isBottom && isRight) {
            currentCursor = Cursor.SE_RESIZE;       // Góc dưới-phải
        } else if (isTop) {
            currentCursor = Cursor.N_RESIZE;        // Cạnh trên
        } else if (isBottom) {
            currentCursor = Cursor.S_RESIZE;        // Cạnh dưới
        } else if (isLeft) {
            currentCursor = Cursor.W_RESIZE;        // Cạnh trái
        } else if (isRight) {
            currentCursor = Cursor.E_RESIZE;        // Cạnh phải
        } else {
            currentCursor = Cursor.DEFAULT;         // Vùng nội dung — con trỏ bình thường
        }

        stage.getScene().setCursor(currentCursor);
    }

    /**
     * Xử lý sự kiện MOUSE_PRESSED: Lưu snapshot vị trí chuột gốc và thông số Stage ban đầu.
     * Chỉ lưu khi cursor đang ở trạng thái resize (không phải DEFAULT) và cửa sổ chưa Maximized.
     */
    private static void handleMousePressed(MouseEvent event, Stage stage) {
        if (currentCursor != Cursor.DEFAULT && !stage.isMaximized()) {
            startMouseX = event.getScreenX();
            startMouseY = event.getScreenY();
            startStageX = stage.getX();
            startStageY = stage.getY();
            startStageWidth = stage.getWidth();
            startStageHeight = stage.getHeight();
        }
    }

    /**
     * Xử lý sự kiện MOUSE_DRAGGED: Thay đổi vị trí (x, y) và kích thước (width, height) mượt mà.
     * <p>
     * Luồng xử lý:
     * 1. Tính delta dịch chuyển chuột so với vị trí nhấn ban đầu.
     * 2. Tùy theo hướng Cursor, điều chỉnh width/height (kéo cạnh Phải/Dưới) hoặc x/y + width/height (kéo cạnh Trái/Trên).
     * 3. Đảm bảo kích thước không nhỏ hơn minWidth/minHeight.
     */
    private static void handleMouseDragged(MouseEvent event, Stage stage, double minWidth, double minHeight) {
        // Bỏ qua nếu đang ở trạng thái cursor mặc định hoặc cửa sổ đang Maximized
        if (currentCursor == Cursor.DEFAULT || stage.isMaximized()) {
            return;
        }

        double deltaX = event.getScreenX() - startMouseX;
        double deltaY = event.getScreenY() - startMouseY;

        // Lấy giá trị minWidth/minHeight lớn nhất giữa tham số truyền vào và thuộc tính Stage
        double effectiveMinWidth = Math.max(minWidth, stage.getMinWidth());
        double effectiveMinHeight = Math.max(minHeight, stage.getMinHeight());

        // --- 1. XỬ LÝ CHIỀU NGANG (TRÁI / PHẢI) ---
        if (currentCursor == Cursor.E_RESIZE || currentCursor == Cursor.NE_RESIZE || currentCursor == Cursor.SE_RESIZE) {
            // Kéo cạnh Phải: Chỉ thay đổi Width, giữ nguyên X
            double newWidth = startStageWidth + deltaX;
            stage.setWidth(Math.max(newWidth, effectiveMinWidth));
        } else if (currentCursor == Cursor.W_RESIZE || currentCursor == Cursor.NW_RESIZE || currentCursor == Cursor.SW_RESIZE) {
            // Kéo cạnh Trái: Thay đổi cả X và Width (X dịch sang trái, Width tăng tương ứng)
            double maxDeltaX = startStageWidth - effectiveMinWidth; // Giới hạn không cho kéo vượt quá minWidth
            double actualDeltaX = Math.min(deltaX, maxDeltaX);
            stage.setX(startStageX + actualDeltaX);
            stage.setWidth(startStageWidth - actualDeltaX);
        }

        // --- 2. XỬ LÝ CHIỀU DỌC (TRÊN / DƯỚI) ---
        if (currentCursor == Cursor.S_RESIZE || currentCursor == Cursor.SE_RESIZE || currentCursor == Cursor.SW_RESIZE) {
            // Kéo cạnh Dưới: Chỉ thay đổi Height, giữ nguyên Y
            double newHeight = startStageHeight + deltaY;
            stage.setHeight(Math.max(newHeight, effectiveMinHeight));
        } else if (currentCursor == Cursor.N_RESIZE || currentCursor == Cursor.NE_RESIZE || currentCursor == Cursor.NW_RESIZE) {
            // Kéo cạnh Trên: Thay đổi cả Y và Height (Y dịch lên trên, Height tăng tương ứng)
            double maxDeltaY = startStageHeight - effectiveMinHeight; // Giới hạn không cho kéo vượt quá minHeight
            double actualDeltaY = Math.min(deltaY, maxDeltaY);
            stage.setY(startStageY + actualDeltaY);
            stage.setHeight(startStageHeight - actualDeltaY);
        }
    }
}
