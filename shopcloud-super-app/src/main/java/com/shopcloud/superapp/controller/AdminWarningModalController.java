package com.shopcloud.superapp.controller;

import com.shopcloud.superapp.store.AdminDataStore;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller điều khiển Modal Pop-up Gửi Cảnh Báo Vi Phạm (Warning Notice).
 * <p>
 * SRP: Tiếp nhận dữ liệu đối tượng vi phạm, validate form nhập cảnh báo và lưu vào AdminDataStore.
 */
public class AdminWarningModalController implements Initializable {

    @FXML
    private Label targetInfoLabel;

    @FXML
    private TextField titleField;

    @FXML
    private TextArea contentArea;

    @FXML
    private ComboBox<String> deadlineComboBox;

    @FXML
    private Button btnCancelWarning;

    @FXML
    private Button btnConfirmSendWarning;

    private String targetId;
    private String targetName;
    private String targetType; // "USER", "SHOP", "PRODUCT"

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        deadlineComboBox.getItems().addAll(
                "24 giờ (Khẩn cấp)",
                "48 giờ",
                "3 ngày",
                "7 ngày"
        );
        deadlineComboBox.getSelectionModel().selectFirst();
    }

    /**
     * Nạp dữ liệu đối tượng bị cảnh báo vào Modal.
     */
    public void setTargetData(String targetId, String targetName, String targetType) {
        this.targetId = targetId;
        this.targetName = targetName;
        this.targetType = targetType;

        String typeText = "PRODUCT".equalsIgnoreCase(targetType) ? "Sản phẩm" :
                ("SHOP".equalsIgnoreCase(targetType) ? "Cửa hàng" : "Người dùng");

        targetInfoLabel.setText(String.format("[%s] %s (%s)", targetId, targetName, typeText));
    }

    @FXML
    private void handleSendWarning(ActionEvent event) {
        String title = titleField.getText() != null ? titleField.getText().trim() : "";
        String content = contentArea.getText() != null ? contentArea.getText().trim() : "";
        String deadline = deadlineComboBox.getValue();

        if (title.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tiêu đề cảnh báo!");
        }

        if (content.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập nội dung chi tiết vi phạm!");
        }

        // Lưu cảnh báo vào Singleton Store
        AdminDataStore.getInstance().sendWarning(targetId, targetName, targetType, title, content, deadline);

        // Hiển thị dialog thông báo thành công
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Gửi cảnh báo");
        alert.setHeaderText(null);
        alert.setContentText("Đã gửi thông báo cảnh báo thành công tới tài khoản [" + targetName + "] với thời hạn " + deadline + "!");
        alert.showAndWait();

        // Đóng window Modal
        closeModal(event);
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeModal(event);
    }

    private void closeModal(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
