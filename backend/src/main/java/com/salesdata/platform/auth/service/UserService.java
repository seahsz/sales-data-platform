package com.salesdata.platform.auth.service;

import com.salesdata.platform.auth.dto.AuthResponse;
import com.salesdata.platform.auth.dto.LoginRequest;
import com.salesdata.platform.auth.dto.RegisterRequest;
import com.salesdata.platform.auth.entity.RefreshTokenEntity;
import com.salesdata.platform.auth.repository.UserRepository;
import com.salesdata.platform.entity.UserEntity;
import com.salesdata.platform.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final RefreshTokenService refreshTokenService;

  @Value("${jwt.access-token.expiration}")
  private Long accessTokenExpiration;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // Find user in database
    UserEntity user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

    return User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .authorities(new ArrayList<>()) // Empty for now, we will add roles later
            .build();
  }

  public AuthResponse register(RegisterRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new RuntimeException("Username is already in use");
    }

    if (userRepository.existsByEmail(request.getEmail())) {
      throw new RuntimeException("Email is already in use");
    }

    // Create new user
    UserEntity userEntity =
        UserEntity.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .build();

    UserEntity savedUserEntity = userRepository.save(userEntity);

    // Generate tokens
    String accessToken = jwtUtil.generateAccessToken(savedUserEntity.getUsername());
    RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(savedUserEntity);

    return new AuthResponse(
        accessToken,
        refreshToken.getToken(),
        savedUserEntity.getUsername(),
        savedUserEntity.getEmail(),
        accessTokenExpiration / 1000);
  }

  public AuthResponse login(LoginRequest request) {
    // Find user by username
    UserEntity user =
        userRepository
            .findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("Invalid username or password"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new RuntimeException("Invalid username or password");
    }

    // Generate tokens
    String accessToken = jwtUtil.generateAccessToken(user.getUsername());
    RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(user);

    return new AuthResponse(
        accessToken,
        refreshToken.getToken(),
        user.getUsername(),
        user.getEmail(),
        accessTokenExpiration / 1000);
  }
}
