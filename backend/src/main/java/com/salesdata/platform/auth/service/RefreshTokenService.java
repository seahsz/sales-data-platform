package com.salesdata.platform.auth.service;

import com.salesdata.platform.auth.entity.RefreshTokenEntity;
import com.salesdata.platform.auth.repository.RefreshTokenRepository;
import com.salesdata.platform.constant.DateTimeConstants;
import com.salesdata.platform.entity.UserEntity;
import com.salesdata.platform.exception.RefreshTokenException;
import com.salesdata.platform.util.JwtUtil;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtUtil jwtUtil;

  @Transactional
  public RefreshTokenEntity createRefreshToken(UserEntity user) {
    refreshTokenRepository.deleteByUser(user);

    String token = jwtUtil.generateRefreshToken();
    LocalDateTime expiryDate =
        jwtUtil
            .getRefreshTokenExpiryDate()
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();

    RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity(token, expiryDate, user);
    return refreshTokenRepository.save(refreshTokenEntity);
  }

  public Optional<RefreshTokenEntity> findByToken(String token) {
    return refreshTokenRepository.findByToken(token);
  }

  public RefreshTokenEntity verifyExpiration(RefreshTokenEntity refreshToken) {
    if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
      refreshTokenRepository.delete(refreshToken);

      String formattedExpiryDate =
          refreshToken.getExpiryDate().format(DateTimeConstants.STANDARD_DATETIME_FORMATTER);
      throw new RefreshTokenException.ExpiredRefreshTokenException(
          "Refresh token expired on " + formattedExpiryDate);
    }
    return refreshToken;
  }

  public RefreshTokenEntity validateRefreshToken(String tokenValue) {
    RefreshTokenEntity refreshToken =
        findByToken(tokenValue)
            .orElseThrow(
                () -> new RefreshTokenException.InvalidRefreshTokenException("Token not found"));
    return verifyExpiration(refreshToken);
  }

  @Transactional
  public void deleteByUser(UserEntity user) {
    refreshTokenRepository.deleteByUser(user);
  }

  @Transactional
  public void cleanupExpiredTokens() {
    refreshTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
  }
}
