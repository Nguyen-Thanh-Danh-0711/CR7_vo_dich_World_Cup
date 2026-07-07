package com.shopcloud.auth.repository;

import com.shopcloud.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Tìm người dùng theo username để phục vụ đăng nhập và xác thực JWT.
    Optional<User> findByUsername(String username);

    // Kiểm tra username đã tồn tại hay chưa khi đăng ký tài khoản mới.
    Boolean existsByUsername(String username);

    // Kiểm tra email đã tồn tại hay chưa khi đăng ký tài khoản mới.
    Boolean existsByEmail(String email);
}
