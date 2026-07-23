package com.shopcloud.superapp.controller;

import com.shopcloud.superapp.model.ViolationReport;
import com.shopcloud.superapp.store.AdminDataStore;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller quản lý Trung Tâm Tiếp Nhận & Xử Lý Báo Cáo Vi Phạm (Violation Reports Center).
 * <p>
 * SRP: Quản lý TableView hiển thị báo cáo vi phạm, xử lý Phản hồi báo cáo, Khóa User/Shop, Xóa sản phẩm vi phạm
 * và mở Modal Pop-up Gửi cảnh báo vi phạm.
 */
public class AdminReportController implements Initializable {

    @FXML
    private ComboBox<String> statusFilterComboBox;

    @FXML
    private TextField searchField;

    @FXML
    private Label pendingCountLabel;

    @FXML
    private TableView<ViolationReport> reportTableView;

    @FXML
    private TableColumn<ViolationReport, String> colReportId;

    @FXML
    private TableColumn<ViolationReport, String> colTargetType;

    @FXML
    private TableColumn<ViolationReport, String> colTargetName;

    @FXML
    private TableColumn<ViolationReport, String> colReporter;

    @FXML
    private TableColumn<ViolationReport, String> colReason;

    @FXML
    private TableColumn<ViolationReport, String> colEvidence;

    @FXML
    private TableColumn<ViolationReport, String> colCreatedAt;

    @FXML
    private TableColumn<ViolationReport, String> colStatus;

    @FXML
    private Button btnReplyReport;

    @FXML
    private Button btnSendWarning;

    @FXML
    private Button btnBanTarget;

    @FXML
    private Button btnDeleteProduct;

    private FilteredList<ViolationReport> filteredReports;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupFiltersAndData();
    }

    private void setupTableColumns() {
        colReportId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReportId()));
        colTargetType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTargetTypeText()));
        colTargetName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTargetName()));
        colReporter.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReporterUsername()));
        colReason.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReason()));
        colEvidence.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEvidence()));
        colCreatedAt.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCreatedAt()));
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatusText()));
    }

    private void setupFiltersAndData() {
        statusFilterComboBox.getItems().addAll("Tất cả", "Chờ xử lý", "Đã xử lý");
        statusFilterComboBox.getSelectionModel().select("Tất cả");

        filteredReports = new FilteredList<>(AdminDataStore.getInstance().getReports(), p -> true);

        statusFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updatePredicate());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> updatePredicate());

        reportTableView.setItems(filteredReports);

        updatePendingBadge();
        AdminDataStore.getInstance().getReports().addListener((javafx.collections.ListChangeListener<ViolationReport>) c -> updatePendingBadge());
    }

    private void updatePredicate() {
        String filterStatus = statusFilterComboBox.getValue();
        String searchText = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";

        filteredReports.setPredicate(report -> {
            boolean matchesStatus = true;
            if ("Chờ xử lý".equals(filterStatus)) {
                matchesStatus = "PENDING".equalsIgnoreCase(report.getStatus());
            } else if ("Đã xử lý".equals(filterStatus)) {
                matchesStatus = "RESOLVED".equalsIgnoreCase(report.getStatus());
            }

            boolean matchesSearch = searchText.isEmpty() ||
                    report.getReportId().toLowerCase().contains(searchText) ||
                    report.getTargetName().toLowerCase().contains(searchText) ||
                    report.getReason().toLowerCase().contains(searchText) ||
                    report.getReporterUsername().toLowerCase().contains(searchText);

            return matchesStatus && matchesSearch;
        });
        updatePendingBadge();
    }

    private void updatePendingBadge() {
        long pendingCount = AdminDataStore.getInstance().getReports().stream()
                .filter(r -> "PENDING".equalsIgnoreCase(r.getStatus()))
                .count();
        pendingCountLabel.setText(String.valueOf(pendingCount));
    }

    /**
     * Xử lý Phản hồi báo cáo vi phạm.
     */
    @FXML
    private void handleReplyReport(ActionEvent event) {
        ViolationReport selected = reportTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            throw new IllegalArgumentException("Vui lòng chọn 1 báo cáo từ bảng để phản hồi!");
        }

        TextInputDialog dialog = new TextInputDialog("Đã tiếp nhận báo cáo và xử lý theo quy định.");
        dialog.setTitle("Phản hồi Báo Cáo");
        dialog.setHeaderText("Gửi lời nhắn phản hồi tới người báo cáo: " + selected.getReporterUsername());
        dialog.setContentText("Nội dung phản hồi:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reply -> {
            AdminDataStore.getInstance().resolveReport(selected.getReportId(), reply);
            reportTableView.refresh();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thành công");
            alert.setHeaderText(null);
            alert.setContentText("Đã gửi phản hồi thành công và chuyển trạng thái báo cáo sang [Đã xử lý]!");
            alert.showAndWait();
        });
    }

    /**
     * Mở Pop-up Modal Gửi cảnh báo vi phạm.
     */
    @FXML
    private void handleSendWarning(ActionEvent event) {
        ViolationReport selected = reportTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            throw new IllegalArgumentException("Vui lòng chọn 1 báo cáo từ bảng để gửi cảnh báo!");
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/AdminWarningModal.fxml"));
            Parent root = loader.load();

            AdminWarningModalController controller = loader.getController();
            controller.setTargetData(selected.getTargetId(), selected.getTargetName(), selected.getTargetType());

            Stage modalStage = new Stage();
            modalStage.setTitle("Gửi Cảnh Báo Vi Phạm");
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setScene(new Scene(root));
            modalStage.setResizable(false);
            modalStage.showAndWait();

        } catch (IOException e) {
            throw new RuntimeException("Không thể nạp giao diện Modal Gửi Cảnh Báo!", e);
        }
    }

    /**
     * Khóa tài khoản User hoặc Shop bị báo cáo.
     */
    @FXML
    private void handleBanTarget(ActionEvent event) {
        ViolationReport selected = reportTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            throw new IllegalArgumentException("Vui lòng chọn 1 báo cáo để thực hiện Khóa tài khoản!");
        }

        String targetType = selected.getTargetType();
        String targetId = selected.getTargetId();
        String targetName = selected.getTargetName();

        if ("USER".equalsIgnoreCase(targetType)) {
            AdminDataStore.getInstance().banUser(targetId);
            showSuccessAlert("Đã chuyển trạng thái tài khoản Người dùng [" + targetName + "] sang BANNED!");
        } else if ("SHOP".equalsIgnoreCase(targetType)) {
            AdminDataStore.getInstance().banShop(targetId);
            showSuccessAlert("Đã chuyển trạng thái Cửa hàng [" + targetName + "] sang BANNED!");
        } else {
            showSuccessAlert("Đối tượng là Sản phẩm. Hãy dùng nút 'Xóa Sản Phẩm Vi Phạm'!");
        }

        // Tự động đánh dấu báo cáo là đã xử lý
        AdminDataStore.getInstance().resolveReport(selected.getReportId(), "Đã thực hiện Khóa tài khoản vi phạm.");
        reportTableView.refresh();
    }

    /**
     * Xóa sản phẩm vi phạm tiêu chuẩn.
     */
    @FXML
    private void handleDeleteProduct(ActionEvent event) {
        ViolationReport selected = reportTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            throw new IllegalArgumentException("Vui lòng chọn 1 báo cáo sản phẩm để xóa!");
        }

        if (!"PRODUCT".equalsIgnoreCase(selected.getTargetType())) {
            throw new IllegalArgumentException("Đối tượng được chọn không phải là Sản phẩm!");
        }

        AdminDataStore.getInstance().deleteProduct(selected.getTargetId());
        AdminDataStore.getInstance().resolveReport(selected.getReportId(), "Đã xóa sản phẩm vi phạm khỏi hệ thống.");

        reportTableView.refresh();
        showSuccessAlert("Đã chuyển sản phẩm [" + selected.getTargetName() + "] sang trạng thái [REMOVED_BY_ADMIN]!");
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo Quản Trị");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
