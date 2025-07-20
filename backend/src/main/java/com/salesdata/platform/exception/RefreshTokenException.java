package com.salesdata.platform.exception;

public class RefreshTokenException extends RuntimeException {

  public RefreshTokenException(String message) {
    super(message);
  }

  public RefreshTokenException(String message, Throwable cause) {
    super(message, cause);
  }

  // Specific exception for expired refresh tokens
  public static class ExpiredRefreshTokenException extends RefreshTokenException {
    public ExpiredRefreshTokenException(String message) {
      super("Refresh token expired: " + message);
    }

    public ExpiredRefreshTokenException(String tokenId, String expiryDate) {
      super(
          "Refresh token expired: Token with"
              + tokenId
              + " expired on "
              + expiryDate
              + ". Please login again");
    }
  }

  public static class InvalidRefreshTokenException extends RefreshTokenException {
    public InvalidRefreshTokenException(String message) {
      super("Invalid refresh token: " + message);
    }
  }

  public static class RevokedRefreshTokenException extends RefreshTokenException {
    public RevokedRefreshTokenException(String message) {
      super("Revoked refresh token: " + message);
    }
  }
}
