package com.salesdata.platform.fileupload;

import com.salesdata.platform.fileupload.dto.CSVProcessingResult;
import com.salesdata.platform.fileupload.dto.FileUploadEntity;
import com.salesdata.platform.fileupload.dto.UserFileStats;
import com.salesdata.platform.fileupload.repository.FileUploadRepository;
import com.salesdata.platform.salesdata.repository.SalesDataRepository;
import com.salesdata.platform.util.CSVProcessor;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class FileUploadService {

  private final FileUploadRepository fileUploadRepository;
  private final SalesDataRepository salesDataRepository;

  private final CSVProcessor csvProcessor;

  @Value("${file.upload.max-size}")
  private long maxFileSize;

  private static final List<String> ALLOWED_EXTENSIONS = List.of(".csv");
  private static final List<String> ALLOWED_CONTENT_TYPES =
      List.of("text/csv", "application/csv", "text/plain");

  public FileUploadEntity uploadFile(MultipartFile file, Long userId) {
    log.info("Starting file upload for user: {} with file: {}", userId, file.getOriginalFilename());

    // Validate file
    validateFile(file);

    // Create file upload record
    FileUploadEntity fileUploadEntity = new FileUploadEntity();
    fileUploadEntity.setUserId(userId);
    fileUploadEntity.setOriginalFilename(file.getOriginalFilename());

    log.info("File upload record created for user: {}", userId);

    // Process CSV file directly from memory
    processCSVFile(fileUploadEntity, file);

    return fileUploadEntity;
  }

  @Transactional(readOnly = true)
  public List<FileUploadEntity> getUserFiles(Long userId) {
    return fileUploadRepository.findByUserIdOrderByCreatedAtDesc(userId);
  }

  @Transactional(readOnly = true)
  public Optional<FileUploadEntity> getFileById(Long fileId, Long userId) {
    return fileUploadRepository.findByIdAndUserId(fileId, userId);
  }

  @Transactional(readOnly = true)
  public UserFileStats getUserFileStatistics(Long userId) {
    return fileUploadRepository.getUserFileStats(userId);
  }

  public void deleteFile(Long fileId, Long userId) {
    Optional<FileUploadEntity> fileUploadOpt =
        fileUploadRepository.findByIdAndUserId(fileId, userId);

    if (fileUploadOpt.isEmpty()) {
      throw new IllegalArgumentException("File not found or access denied");
    }

    FileUploadEntity fileUploadEntity = fileUploadOpt.get();

    log.info("Deleting file upload: {} for user: {}", fileId, userId);

    // Delete associated sales records
    int deletedRecords = salesDataRepository.deleteByFileUploadId(fileId);
    log.info("Deleted {} sales records associated with file upload: {}", deletedRecords, fileId);

    // Delete file upload record
    fileUploadRepository.delete(fileUploadEntity);
    log.info("File upload record deleted: {}", fileId);
  }

  @Transactional(readOnly = true)
  public String getProcessingStatus(Long fileId, Long userId) {
    Optional<FileUploadEntity> fileUploadOpt =
        fileUploadRepository.findByIdAndUserId(fileId, userId);
    if (fileUploadOpt.isEmpty()) {
      return "File not found";
    }

    FileUploadEntity fileUploadEntity = fileUploadOpt.get();
    return switch (fileUploadEntity.getUploadStatus()) {
      case PENDING -> "Pending processing";
      case PROCESSING ->
          String.format(
              "Processing... (%d/%d records",
              fileUploadEntity.getRecordsProcessed(), fileUploadEntity.getTotalRows());
      case COMPLETED ->
          String.format(
              "Completed: %d successful, %d failed",
              fileUploadEntity.getRecordsProcessed(), fileUploadEntity.getRecordsFailed());
      case FAILED -> "Failed: " + fileUploadEntity.getErrorMessage();
    };
  }

  private void validateFile(MultipartFile file) throws IllegalArgumentException {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File cannot be empty");
    }

    // Check file size - if we have an upper limit set for file size
    if (file.getSize() > maxFileSize) {
      throw new IllegalArgumentException("File size exceeds maximum allowed size");
    }

    // Check file extension
    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null || originalFilename.trim().isEmpty()) {
      throw new IllegalArgumentException("Invalid filename");
    }

    String fileExtension = getFileExtension(originalFilename).toLowerCase();
    if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
      throw new IllegalArgumentException(
          String.format(
              "Invalid file type, Allowed types: %s", String.join(", ", ALLOWED_EXTENSIONS)));
    }

    // Check content type
    String contentType = file.getContentType();
    if (contentType != null && !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
      log.warn("Unexpected content type: {} for file: {}", contentType, originalFilename);
      // Don't throw error, just log warning as content type can be unreliable
    }
  }

  private String getFileExtension(String filename) {
    if (filename == null || filename.lastIndexOf('.') == -1) {
      return "";
    }
    return filename.substring(filename.lastIndexOf('.'));
  }

  private void processCSVFile(FileUploadEntity fileUploadEntity, MultipartFile multipartFile) {
    try {
      log.info("Starting CSV processing for file upload: {}", fileUploadEntity.getId());

      // Update status to processing
      fileUploadEntity.markAsProcessing();
      fileUploadRepository.save(fileUploadEntity);

      // Process CSV directly from MultipartFile InputStream
      CSVProcessingResult result =
          csvProcessor.processCSVFile(
              multipartFile.getInputStream(),
              fileUploadEntity.getUserId(),
              fileUploadEntity.getId());

      // Update file upload with processing results
      fileUploadEntity.setTotalRows(result.totalRows());
      fileUploadEntity.setRecordsProcessed(result.getSuccessfulCount());
      fileUploadEntity.setRecordsFailed(result.getFailedCount());

      if (result.hasFatalError()) {
        // Fatal error - mark as failed
        fileUploadEntity.markAsFailed(result.fatalError());
        log.error(
            "CSV processing failed with fatal error for file {}: {}",
            fileUploadEntity.getId(),
            result.fatalError());

      } else {
        // Save successful records to database
        if (!result.successfulRecords().isEmpty()) {
          salesDataRepository.saveAll(result.successfulRecords());
          log.info("Saved {} sales records to database", result.successfulRecords().size());
        }

        // Mark as completed (even if some records failed)
        fileUploadEntity.markAsCompleted();

        // Set error message if there were partial failures
        if (result.hasErrors()) {
          String errorSummary =
              String.format(
                  "%d records failed validation. First few errors: %s",
                  result.getFailedCount(),
                  String.join(
                      "; ",
                      result
                          .errorMessages()
                          .subList(0, Math.min(3, result.errorMessages().size()))));
          fileUploadEntity.setErrorMessage(errorSummary);
        }
      }

    } catch (Exception e) {
      log.error(
          "Unexpected error during CSV processing for file {}: {}",
          fileUploadEntity.getId(),
          e.getMessage(),
          e);
      fileUploadEntity.markAsFailed("Unexpected error during processing: " + e.getMessage());
    } finally {
      // Always save the final state
      fileUploadRepository.saveAndFlush(fileUploadEntity);
    }
  }
}
