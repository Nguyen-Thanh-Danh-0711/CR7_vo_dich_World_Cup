package main.java.com.shopcloud.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String passwordHash; // Mật khẩu đã mã hóa lưu trữ bảo mật

    @Column(unique = true, nullable = false)
    private String email;

    // Lưu danh sách các vai trò (Roles) để JavaFX nhận diện Không gian (Space) tương ứng
    // Dữ liệu nhỏ nhưng lúc nào cũng dùng
    // Khi user đăng nhập, hệ thống gần như cần biết vai trò của họ
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles; // Chứa dữ liệu dạng: ROLE_BUYER, ROLE_SELLER, ROLE_ADMIN

    @Column(nullable = false)
    private String status; // Trạng thái tài khoản: ACTIVE, LOCKED
}
