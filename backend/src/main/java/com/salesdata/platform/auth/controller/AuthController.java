package com.salesdata.platform.auth.controller;

import com.salesdata.platform.auth.dto.AuthResponse;
import com.salesdata.platform.auth.dto.LoginRequest;
import com.salesdata.platform.auth.dto.RefreshTokenRequest;
import com.salesdata.platform.auth.dto.RegisterRequest;
import com.salesdata.platform.auth.entity.RefreshTokenEntity;
import com.salesdata.platform.auth.service.RefreshTokenService;
import com.salesdata.platform.auth.service.UserService;
import com.salesdata.platform.exception.JwtTokenException;
import com.salesdata.platform.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserService userService;
  private final RefreshTokenService refreshTokenService;
  private final JwtUtil jwtUtil;

  @Value("${jwt.access-token.expiration}")
  private Long accessTokenExpiration;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    AuthResponse response = userService.register(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    AuthResponse response = userService.login(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
    // Validate refresh token
    RefreshTokenEntity refreshToken =
        refreshTokenService.validateRefreshToken(request.getRefreshToken());

    // Generate new access token
    String accessToken = jwtUtil.generateAccessToken(refreshToken.getUser().getUsername());

    AuthResponse response =
        new AuthResponse(
            accessToken,
            refreshToken.getToken(),
            refreshToken.getUser().getUsername(),
            refreshToken.getUser().getEmail(),
            accessTokenExpiration / 1000);

    return ResponseEntity.ok(response);
  }

  @PostMapping("/logout")
  public ResponseEntity<String> logout(@Valid @RequestBody RefreshTokenRequest request) {
    // Find and delete the refresh token
    refreshTokenService
        .findByToken(request.getRefreshToken())
        .ifPresentOrElse(
            token -> refreshTokenService.deleteByUser(token.getUser()),
            () -> {
              throw new JwtTokenException.InvalidTokenException("Invalid refresh token");
            });

    return ResponseEntity.ok("Logged out successfully");
  }
}
