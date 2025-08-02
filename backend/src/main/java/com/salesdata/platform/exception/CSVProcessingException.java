package com.salesdata.platform.exception;

public class CSVProcessingException extends Exception {

  public CSVProcessingException(String message) {
    super(message);
  }

  public CSVProcessingException(String message, Throwable cause) {
    super(message, cause);
  }
}
