package com.salesdata.platform.util;

import com.salesdata.platform.exception.JwtTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.access-token.expiration}")
  private String accessTokenExpiration;

  @Value("${jwt.refresh-token.expiration}")
  private String refreshTokenExpiration;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes());
  }

  // Generate Access Token (short-lived)
  public String generateAccessToken(String username) {
    Instant now = Instant.now();
    Instant expiryInstant = now.plusMillis(Long.parseLong(accessTokenExpiration));
    return Jwts.builder()
        .subject(username)
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiryInstant))
        .claim("type", "access")
        .signWith(getSigningKey())
        .compact();
  }

  // Generate Refresh Token (long-lived, random UUID)
  public String generateRefreshToken() {
    return UUID.randomUUID().toString();
  }

  // Central method to extract claims safely
  private Claims getClaimsFromToken(String token) {
    if (token == null || token.trim().isEmpty()) {
      throw new JwtTokenException.InvalidTokenException("JWT token cannot be null or empty");
    }

    try {
      return Jwts.parser()
          .verifyWith(getSigningKey())
          .build()
          .parseSignedClaims(token)
          .getPayload();
    } catch (JwtTokenException.ExpiredTokenException ex) {
      throw new JwtTokenException.ExpiredTokenException(ex.getMessage());
    } catch (JwtTokenException.MalformedTokenException ex) {
      throw new JwtTokenException.MalformedTokenException(ex.getMessage());
    } catch (JwtException | IllegalArgumentException ex) {
      throw new JwtTokenException.InvalidTokenException(ex.getMessage(), ex);
    }
  }

  // Key method to check if token is valid -> same username + not expired
  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = getUsernameFromToken(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }

  // Generic method to extract any claim
  private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = getClaimsFromToken(token);
    return claimsResolver.apply(claims);
  }

  public String getUsernameFromToken(String token) {
    return getClaimFromToken(token, Claims::getSubject);
  }

  public Date getExpirationDateFromToken(String token) {
    return getClaimFromToken(token, Claims::getExpiration);
  }

  public Date getIssuedAtFromToken(String token) {
    return getClaimFromToken(token, Claims::getIssuedAt);
  }

  public String getTokenTypeFromToken(String token) {
    return getClaimFromToken(token, claims -> claims.get("type", String.class));
  }

  public boolean validateAccessToken(String token) {
    try {
      String tokenType = getTokenTypeFromToken(token);
      return "access".equals(tokenType);
    } catch (JwtTokenException ex) {
      return false;
    }
  }

  public Date getRefreshTokenExpiryDate() {
    return new Date(System.currentTimeMillis() + Long.parseLong(refreshTokenExpiration));
  }

  private boolean isTokenExpired(String token) {
    return getExpirationDateFromToken(token).before(new Date());
  };
}
