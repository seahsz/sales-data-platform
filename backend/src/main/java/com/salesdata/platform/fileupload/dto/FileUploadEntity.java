package com.salesdata.platform.fileupload.dto;

import com.salesdata.platform.fileupload.enums.UploadStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "file_uploads")
public class FileUploadEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "original_filename", nullable = false)
  private String originalFilename;

  @Enumerated(EnumType.STRING)
  @Column(name = "upload_status", nullable = false)
  private UploadStatus uploadStatus;

  @Column(name = "total_rows", nullable = false)
  private Integer totalRows = 0;

  @Column(name = "records_processed", nullable = false)
  private Integer recordsProcessed = 0;

  @Column(name = "records_failed", nullable = false)
  private Integer recordsFailed = 0;

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "processed_at")
  private LocalDateTime processedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    if (uploadStatus == null) {
      uploadStatus = UploadStatus.PENDING;
    }
  }

  // Helper methods
  public boolean isProcessingComplete() {
    return uploadStatus == UploadStatus.COMPLETED || uploadStatus == UploadStatus.FAILED;
  }

  public double getSuccessRate() {
    if (totalRows == 0) return 0.0;
    return (double) recordsProcessed / totalRows * 100;
  }

  public void markAsProcessing() {
    uploadStatus = UploadStatus.PROCESSING;
  }

  public void markAsCompleted() {
    uploadStatus = UploadStatus.COMPLETED;
    processedAt = LocalDateTime.now();
  }

  public void markAsFailed(String errorMessage) {
    uploadStatus = UploadStatus.FAILED;
    this.errorMessage = errorMessage;
    processedAt = LocalDateTime.now();
  }
}
