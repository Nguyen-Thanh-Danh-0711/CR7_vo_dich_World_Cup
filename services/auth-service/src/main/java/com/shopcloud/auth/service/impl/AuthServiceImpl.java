package com.shopcloud.auth.service.impl;

import com.shopcloud.auth.dto.AuthResponse;
import com.shopcloud.auth.dto.LoginRequest;
import com.shopcloud.auth.dto.RegisterRequest;
import com.shopcloud.auth.entity.User;
import com.shopcloud.auth.repository.UserRepository;
import com.shopcloud.auth.security.JwtTokenProvider;
import com.shopcloud.auth.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_ROLE = "ROLE_BUYER";
    private static final String ACTIVE_STATUS = "ACTIVE";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        if (Boolean.TRUE.equals(userRepository.existsByUsername(registerRequest.getUsername()))) {
            throw new RuntimeException("Username already exists: " + registerRequest.getUsername());
        }

        if (Boolean.TRUE.equals(userRepository.existsByEmail(registerRequest.getEmail()))) {
            throw new RuntimeException("Email already exists: " + registerRequest.getEmail());
        }

        Set<String> roles = resolveRoles(registerRequest.getRoles());

        User user = User.builder()
                .username(registerRequest.getUsername())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .roles(roles)
                .status(ACTIVE_STATUS)
                .build();

        User savedUser = userRepository.save(user);
        String accessToken = jwtTokenProvider.generateToken(savedUser);

        return buildAuthResponse(savedUser, accessToken);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid username or password");
        }

        String accessToken = jwtTokenProvider.generateToken(user);

        return buildAuthResponse(user, accessToken);
    }

    private Set<String> resolveRoles(Set<String> requestedRoles) {
        if (requestedRoles == null || requestedRoles.isEmpty()) {
            Set<String> defaultRoles = new HashSet<>();
            defaultRoles.add(DEFAULT_ROLE);
            return defaultRoles;
        }

        return new HashSet<>(requestedRoles);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .username(user.getUsername())
                .roles(user.getRoles())
                .build();
    }
}
