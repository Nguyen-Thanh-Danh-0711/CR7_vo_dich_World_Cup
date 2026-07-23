package com.shopcloud.superapp.model;

/**
 * Model đại diện cho thông tin Người Dùng trong phân hệ Quản Trị Admin.
 * <p>
 * SRP: Quản lý dữ liệu người dùng (Mã ND, Tên người dùng, Email, Vai trò, Trạng thái hoạt động/bị cấm).
 * Không chứa thuộc tính số điện thoại theo thiết kế hệ thống.
 */
public class User {

    private String id;
    private String username;
    private String email;
    private String role; // "Buyer", "Seller", "Admin"
    private String status; // "ACTIVE", "BANNED"

    public User() {
    }

    public User(String id, String username, String email, String role, String status) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isBanned() {
        return "BANNED".equalsIgnoreCase(status);
    }

    /**
     * Trả về văn bản hiển thị trạng thái tiếng Việt thân thiện với người dùng.
     */
    public String getStatusText() {
        if ("BANNED".equalsIgnoreCase(status)) {
            return "Bị cấm (BANNED)";
        }
        return "Hoạt động";
    }
}
