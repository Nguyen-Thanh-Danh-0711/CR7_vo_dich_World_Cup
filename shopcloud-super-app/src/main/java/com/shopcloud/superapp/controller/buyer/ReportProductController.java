package com.shopcloud.superapp.controller.buyer;

import com.shopcloud.superapp.App;
import com.shopcloud.superapp.model.Product;
import com.shopcloud.superapp.model.ViolationReport;
import com.shopcloud.superapp.store.AdminDataStore;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controller cho Modal tố cáo sản phẩm vi phạm (Report Product Modal).
 * <p>
 * Trách nhiệm theo SRP: Quản lý form tố cáo (chọn lý do, mô tả),
 * validate dữ liệu, tạo ViolationReport mới và gửi vào AdminDataStore.
 * Không xử lý ngoại lệ tại chỗ — đẩy lên GlobalExceptionHandler.
 */
public class ReportProductController implements Initializable {

    @FXML
    private HBox headerBar;

    @FXML
    private Label lblProductName;

    @FXML
    private ComboBox<String> reasonComboBox;

    @FXML
    private TextArea descriptionArea;

    /** Sản phẩm đang bị tố cáo. */
    private Product product;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Khởi tạo danh sách lý do tố cáo
        reasonComboBox.setItems(FXCollections.observableArrayList(
                "Hàng giả / Hàng nhái",
                "Mô tả sai sự thật",
                "Nội dung vi phạm pháp luật",
                "Hình ảnh không phù hợp",
                "Lý do khác"
        ));

        // Gắn kéo thả header
        setupDraggableHeader();
    }

    /**
     * Thiết lập sản phẩm đang bị tố cáo.
     */
    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            lblProductName.setText("Sản phẩm: " + product.getName() + " (" + product.getId() + ")");
        }
    }

    // ========================================================================================
    // GỬI TỐ CÁO (SUBMIT REPORT)
    // ========================================================================================

    /**
     * Xử lý gửi tố cáo sản phẩm.
     * Validate: phải chọn lý do tố cáo.
     * Tạo ViolationReport mới và thêm vào AdminDataStore.
     */
    @FXML
    private void handleSubmitReport(ActionEvent event) throws Exception {
        // 1. Validate lý do tố cáo
        String reason = reasonComboBox.getValue();
        if (reason == null || reason.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn lý do tố cáo trước khi gửi!");
        }

        // 2. Lấy mô tả bổ sung (tùy chọn)
        String description = descriptionArea.getText() != null ? descriptionArea.getText().trim() : "";
        String fullReason = description.isEmpty() ? reason : reason + " — " + description;

        // 3. Tạo ViolationReport mới
        String reportId = "RPT" + System.currentTimeMillis();
        String reporter = App.UserSession.getUsername() != null ? App.UserSession.getUsername() : "anonymous";
        String createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        ViolationReport report = new ViolationReport(
                reportId, reporter, "PRODUCT",
                product != null ? product.getId() : "UNKNOWN",
                product != null ? product.getName() : "Không rõ",
                fullReason, "", "PENDING", createdAt
        );

        // 4. Thêm vào AdminDataStore
        AdminDataStore.getInstance().addReport(report);

        // 5. Đóng modal
        closeStage();
    }

    // ========================================================================================
    // ĐIỀU KHIỂN CỬA SỔ (WINDOW CONTROLS)
    // ========================================================================================

    @FXML
    private void handleCloseWindow(ActionEvent event) {
        closeStage();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeStage();
    }

    private void closeStage() {
        Stage stage = getModalStage();
        if (stage != null) {
            stage.close();
        }
    }

    private Stage getModalStage() {
        if (headerBar != null && headerBar.getScene() != null) {
            return (Stage) headerBar.getScene().getWindow();
        }
        return null;
    }

    /**
     * Gắn sự kiện kéo thả chuột lên Header Bar.
     */
    private void setupDraggableHeader() {
        if (headerBar == null) {
            return;
        }
        final double[] dragOffset = new double[2];

        headerBar.setOnMousePressed(event -> {
            Stage stage = getModalStage();
            if (stage != null) {
                dragOffset[0] = stage.getX() - event.getScreenX();
                dragOffset[1] = stage.getY() - event.getScreenY();
            }
        });

        headerBar.setOnMouseDragged(event -> {
            Stage stage = getModalStage();
            if (stage != null) {
                stage.setX(event.getScreenX() + dragOffset[0]);
                stage.setY(event.getScreenY() + dragOffset[1]);
            }
        });
    }
}
