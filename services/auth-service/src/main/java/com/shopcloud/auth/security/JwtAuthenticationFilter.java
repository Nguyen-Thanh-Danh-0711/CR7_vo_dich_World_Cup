package com.shopcloud.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String jwt = resolveToken(request); // Lấy JWT

        if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) { // Kiểm tra token
            // validateToken(jwt): Kiểm tra xem chữ ký đúng, token còn hạn, có bị sửa hay ko
            String username = jwtTokenProvider.getUsernameFromJWT(jwt); // Lấy username
            UserDetails userDetails = userDetailsService.loadUserByUsername(username); // Lấy UserDetails

            UsernamePasswordAuthenticationToken authenticationToken =
            //Tạo Authentication
            // Đại diện cho người dùng đã xác thực
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                            // Thêm thông tin request

            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            // Lưu thông tin người dùng u=của request hiện đại
        }

        filterChain.doFilter(request, response); // Cho request đi tiếp
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization"); // Đọc header

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Kiểm tra chuẩn JWT
        }

        return null;
    }
}
