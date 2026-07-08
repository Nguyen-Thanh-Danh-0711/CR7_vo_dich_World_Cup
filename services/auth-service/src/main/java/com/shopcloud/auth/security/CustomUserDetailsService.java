package com.shopcloud.auth.security;

import com.shopcloud.auth.entity.User;
import com.shopcloud.auth.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
        // inject userRepository để truy vấn database
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return org.springframework.security.core.userdetails.User.builder() // Không phải entity User
        // Spring Security không làm việc trực tiếp với entity
        // Mà làm việc với UserDetails
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(user.getRoles().stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet())) // Thu lại các quyền thành 1 set
                .build(); // Tạo ra đối tượng UserDetails
                // Spring Security sẽ dùng chính đối tượng này để
                // Kiểm tra mật khẩu.
                // Kiểm tra quyền.
                // Lưu thông tin xác thực trong SecurityContext
    }
}
