package com.shopcloud.superapp.model;

/**
 * Model đại diện cho Thông Báo Cảnh Báo Vi Phạm (Warning Notice) được Admin tạo và gửi cho tài khoản bị cảnh báo.
 * <p>
 * SRP: Lưu giữ chi tiết nội dung cảnh báo, tiêu đề, đối tượng nhận và thời hạn khắc phục.
 */
public class WarningNotice {

    private String id;
    private String targetId;
    private String targetName;
    private String targetType; // "USER", "SHOP", "PRODUCT"
    private String title;
    private String content;
    private String deadline;
    private String createdAt;

    public WarningNotice() {
    }

    public WarningNotice(String id, String targetId, String targetName, String targetType, String title, String content, String deadline, String createdAt) {
        this.id = id;
        this.targetId = targetId;
        this.targetName = targetName;
        this.targetType = targetType;
        this.title = title;
        this.content = content;
        this.deadline = deadline;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
