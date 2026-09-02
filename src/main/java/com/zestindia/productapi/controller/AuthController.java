package com.zestindia.productapi.controller;

import com.zestindia.productapi.dto.AuthRequest;
import com.zestindia.productapi.dto.AuthResponse;
import com.zestindia.productapi.dto.RefreshTokenRequest;
import com.zestindia.productapi.dto.RegisterRequest;
import com.zestindia.productapi.entity.RefreshToken;
import com.zestindia.productapi.entity.User;
import com.zestindia.productapi.exception.DuplicateResourceException;
import com.zestindia.productapi.exception.TokenRefreshException;
import com.zestindia.productapi.repository.UserRepository;
import com.zestindia.productapi.security.JwtUtil;
import com.zestindia.productapi.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication endpoints issuing JWT access & refresh tokens")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    @Operation(summary = "Create a new user account (defaults to ROLE_USER)")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }

        User.Role role = User.Role.ROLE_USER;
        if (StringUtils.hasText(request.getRole()) && "ADMIN".equalsIgnoreCase(request.getRole().trim())) {
            role = User.Role.ROLE_ADMIN;
        }

        User newUser = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        User savedUser = userRepository.save(newUser);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getUsername());
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build());
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and obtain an access token + refresh token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalStateException("User vanished after authentication"));

        String accessToken = jwtUtil.generateAccessToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build());
    }

    @PostMapping("/refresh")
    @Operation(summary = "Exchange a valid refresh token for a new access token (refresh token rotation)")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new TokenRefreshException(request.getRefreshToken(), "Refresh token not found"));

        RefreshToken validToken = refreshTokenService.verifyExpiration(storedToken);
        User user = validToken.getUser();

        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String newAccessToken = jwtUtil.generateAccessToken(userDetails);

        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .build());
    }
}