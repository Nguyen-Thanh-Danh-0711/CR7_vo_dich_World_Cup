package com.shopcloud.superapp.model;

/**
 * Model đại diện cho Báo cáo Vi phạm gửi về Trung tâm Báo cáo (Violation Reports Center).
 * <p>
 * SRP: Lưu thông tin chi tiết báo cáo vi phạm (Mã báo cáo, Người báo cáo, Đối tượng vi phạm, Lý do, Bằng chứng, Trạng thái).
 */
public class ViolationReport {

    private String reportId;
    private String reporterUsername;
    private String targetType; // "USER", "SHOP", "PRODUCT"
    private String targetId;
    private String targetName;
    private String reason;
    private String evidence; // Đường dẫn ảnh hoặc mô tả bằng chứng
    private String status; // "PENDING" (Chờ xử lý), "RESOLVED" (Đã xử lý)
    private String adminReply; // Lời phản hồi từ Admin gửi người báo cáo
    private String createdAt;

    public ViolationReport() {
    }

    public ViolationReport(String reportId, String reporterUsername, String targetType, String targetId,
                           String targetName, String reason, String evidence, String status, String createdAt) {
        this.reportId = reportId;
        this.reporterUsername = reporterUsername;
        this.targetType = targetType;
        this.targetId = targetId;
        this.targetName = targetName;
        this.reason = reason;
        this.evidence = evidence;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getReporterUsername() {
        return reporterUsername;
    }

    public void setReporterUsername(String reporterUsername) {
        this.reporterUsername = reporterUsername;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getEvidence() {
        return evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminReply() {
        return adminReply;
    }

    public void setAdminReply(String adminReply) {
        this.adminReply = adminReply;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getTargetTypeText() {
        if ("USER".equalsIgnoreCase(targetType)) {
            return "Người dùng";
        } else if ("SHOP".equalsIgnoreCase(targetType)) {
            return "Cửa hàng";
        } else if ("PRODUCT".equalsIgnoreCase(targetType)) {
            return "Sản phẩm";
        }
        return targetType;
    }

    public String getStatusText() {
        if ("RESOLVED".equalsIgnoreCase(status)) {
            return "Đã xử lý";
        }
        return "Chờ xử lý";
    }
}
