package com.salesdata.platform.fileupload.dto;

import com.salesdata.platform.salesdata.dto.SalesDataEntity;
import java.util.List;

public record CSVProcessingResult(
    List<SalesDataEntity> successfulRecords,
    List<String> errorMessages,
    int totalRows,
    String fatalError) {

  public boolean hasFatalError() {
    return fatalError != null;
  }

  public boolean hasErrors() {
    return !errorMessages.isEmpty();
  }

  public int getSuccessfulCount() {
    return successfulRecords.size();
  }

  public int getFailedCount() {
    return errorMessages.size();
  }

  public boolean isCompleteSuccess() {
    return !hasFatalError() && !hasErrors();
  }

  public double getSuccessRate() {
    if (totalRows == 0) return 0.0;
    return (double) getSuccessfulCount() / totalRows * 100;
  }
}
