package com.salesdata.platform.exception.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

  private String errorCode;
  private String message;
  private LocalDateTime timestamp;
}
