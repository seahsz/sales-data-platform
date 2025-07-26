package com.salesdata.platform.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
  private String accessToken;
  private String refreshToken;
  private String type = "Bearer";
  private String username;
  private String email;
  private Long expiresIn; // Access token expiration in seconds

  public AuthResponse(
      String accessToken, String refreshToken, String username, String email, Long expiresIn) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.username = username;
    this.email = email;
    this.expiresIn = expiresIn;
  }
}
