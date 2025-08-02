package com.salesdata.platform.fileupload.dto;

public record UserFileStats(
    Long totalFiles, Long totalRecordsProcessed, Long successfulUploads, Long failedUploads) {

  public Long pendingUploads() {
    return totalFiles - successfulUploads - failedUploads;
  }

  public double successRate() {
    if (totalFiles == 0) return 0.0;
    return (double) successfulUploads / totalFiles * 100;
  }

  public boolean hasFailures() {
    return failedUploads > 0;
  }

  public boolean hasUploads() {
    return totalFiles > 0;
  }
}
