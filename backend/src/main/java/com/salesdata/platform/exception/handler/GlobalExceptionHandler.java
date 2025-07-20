package com.salesdata.platform.exception.handler;

import com.salesdata.platform.exception.JwtTokenException;
import com.salesdata.platform.exception.RefreshTokenException;
import com.salesdata.platform.exception.dto.ErrorResponse;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // Handle JWT token exceptions
  @ExceptionHandler(JwtTokenException.class)
  public ResponseEntity<ErrorResponse> handleJwtTokenException(JwtTokenException e) {
    ErrorResponse error = new ErrorResponse("JWT_ERROR", e.getMessage(), LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  @ExceptionHandler(RefreshTokenException.class)
  public ResponseEntity<ErrorResponse> handleRefreshTokenException(RefreshTokenException e) {
    ErrorResponse error =
        new ErrorResponse("REFRESH_TOKEN_ERROR", e.getMessage(), LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
    ErrorResponse error = new ErrorResponse("RUNTIME_ERROR", e.getMessage(), LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }
}
