package com.shopcloud.superapp.exception;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Bộ xử lý lỗi tập trung (Centralized Error Handling) cho toàn bộ ứng dụng.
 * <p>
 * Trách nhiệm theo SRP: Đón nhận mọi {@link Throwable} không được bắt từ bất kỳ
 * Thread nào, ghi log ra console và hiển thị Alert an toàn trên UI Thread.
 * Tách biệt hoàn toàn khỏi vòng đời UI chính và logic nghiệp vụ của Controller.
 */
public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final String USER_SAFE_MESSAGE =
            "Đã xảy ra sự cố không mong muốn. Vui lòng thử lại hoặc liên hệ bộ phận hỗ trợ ShopCloud.";

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        // Ghi log kỹ thuật phục vụ dev/ops — không phụ thuộc UI Thread
        System.err.printf("[GlobalExceptionHandler] Thread=%s%n", thread.getName());
        throwable.printStackTrace();

        // Alert phải chạy trên JavaFX Application Thread
        Platform.runLater(() -> showErrorDialog(throwable));
    }

    /**
     * Hiển thị hộp thoại lỗi với nội dung mở rộng chứa StackTrace đầy đủ.
     */
    private void showErrorDialog(Throwable throwable) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ShopCloud — Lỗi hệ thống");
        alert.setHeaderText("Rất tiếc, ứng dụng gặp sự cố");
        alert.setContentText(USER_SAFE_MESSAGE);

        // Vùng Expandable Content: StackTrace chi tiết cho debug kỹ thuật
        TextArea stackTraceArea = new TextArea(buildStackTrace(throwable));
        stackTraceArea.setEditable(false);
        stackTraceArea.setWrapText(true);
        stackTraceArea.setPrefRowCount(12);

        GridPane.setVgrow(stackTraceArea, Priority.ALWAYS);
        GridPane.setHgrow(stackTraceArea, Priority.ALWAYS);

        GridPane expandablePane = new GridPane();
        expandablePane.setMaxWidth(Double.MAX_VALUE);
        expandablePane.add(new Label("Chi tiết kỹ thuật (StackTrace):"), 0, 0);
        expandablePane.add(stackTraceArea, 0, 1);
        GridPane.setMargin(stackTraceArea, new Insets(8, 0, 0, 0));

        alert.getDialogPane().setExpandableContent(expandablePane);
        alert.getDialogPane().setExpanded(true);
        alert.showAndWait();
    }

    /**
     * Chuyển Throwable thành chuỗi StackTrace đầy đủ (bao gồm cause chain).
     */
    private String buildStackTrace(Throwable throwable) {
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
