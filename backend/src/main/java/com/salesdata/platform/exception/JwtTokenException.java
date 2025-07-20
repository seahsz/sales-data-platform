package com.salesdata.platform.exception;

public class JwtTokenException extends RuntimeException {

  public JwtTokenException(String message) {
    super(message);
  }

  public JwtTokenException(String message, Throwable cause) {
    super(message, cause);
  }

  // Specific exception types for different JWT errors
  public static class InvalidTokenException extends JwtTokenException {
    public InvalidTokenException(String message) {
      super("Invalid JWT token: " + message);
    }

    public InvalidTokenException(String message, Throwable cause) {
      super("Invalid JWT token: " + message, cause);
    }
  }

  public static class ExpiredTokenException extends JwtTokenException {
    public ExpiredTokenException(String message) {
      super("Expired JWT token: " + message);
    }
  }

  public static class MalformedTokenException extends JwtTokenException {
    public MalformedTokenException(String message) {
      super("Malformed JWT token: " + message);
    }
  }
}
