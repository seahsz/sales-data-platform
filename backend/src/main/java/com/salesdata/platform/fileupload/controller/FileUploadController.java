package com.salesdata.platform.fileupload.controller;

import com.salesdata.platform.auth.annotation.CurrentUser;
import com.salesdata.platform.auth.dto.CustomUserDetails;
import com.salesdata.platform.fileupload.FileUploadService;
import com.salesdata.platform.fileupload.dto.FileUploadEntity;
import com.salesdata.platform.fileupload.dto.UserFileStats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "http://localhost:3000")
public class FileUploadController {

  private final FileUploadService fileUploadService;

  private static final String MESSAGE_CONSTANT = "message";
  private static final String SUCCESS_CONSTANT = "success";

  /** Upload a CSV file */
  @PostMapping("/upload")
  public ResponseEntity<Map<String, Object>> uploadFile(
          @RequestParam("file") MultipartFile file, @CurrentUser CustomUserDetails userDetails) {

    Map<String, Object> response = new HashMap<>();

    try {
      Long userId = userDetails.getId();

      log.info("File upload request from user: {} for file: {}", userId, file.getOriginalFilename());

      // Upload and process file
      FileUploadEntity fileUploadEntity = fileUploadService.uploadFile(file, userId);

      response.put(SUCCESS_CONSTANT, true);
      response.put(MESSAGE_CONSTANT, "File uploaded successfully");
      response.put("fileId", fileUploadEntity.getId());
      response.put("fileName", file.getOriginalFilename());
      response.put("status", fileUploadEntity.getUploadStatus().toString());
      response.put("uploadedAt", fileUploadEntity.getCreatedAt());
      response.put("totalRows", fileUploadEntity.getTotalRows());
      response.put("recordsProcessed", fileUploadEntity.getRecordsProcessed());
      response.put("recordsFailed", fileUploadEntity.getRecordsFailed());

      return ResponseEntity.ok(response);

    } catch (IllegalArgumentException e) {
      log.warn("File upload validation error: {}", e.getMessage());
      response.put(SUCCESS_CONSTANT, false);
      response.put(MESSAGE_CONSTANT, e.getMessage());
      return ResponseEntity.badRequest().body(response);

    } catch (Exception e) {
      log.error("Unexpected error during file upload: {}", e.getMessage(), e);
      response.put(SUCCESS_CONSTANT, false);
      response.put(MESSAGE_CONSTANT, "Unexpected error occurred");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> getUserFiles(@CurrentUser CustomUserDetails userDetails) {
    Map<String, Object> response = new HashMap<>();

    try {
      Long userId = userDetails.getId();
      List<FileUploadEntity> files = fileUploadService.getUserFiles(userId);

      response.put(SUCCESS_CONSTANT, true);
      response.put("files", files);
      response.put("count",  files.size());

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Error retrieving user files: {}", e.getMessage(), e);
      response.put(SUCCESS_CONSTANT, false);
      response.put(MESSAGE_CONSTANT, "Error retrieving files");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /**
   * Get specific file details
   */
  @GetMapping("/{fileId}")
  public ResponseEntity<Map<String, Object>> getFileDetails(
          @PathVariable Long fileId, @CurrentUser CustomUserDetails userDetails) {

    Map<String, Object> response = new HashMap<>();

    try {
      Long userId = userDetails.getId();
      Optional<FileUploadEntity> fileOpt = fileUploadService.getFileById(fileId, userId);

      if (fileOpt.isEmpty()) {
        response.put(SUCCESS_CONSTANT, false);
        response.put(MESSAGE_CONSTANT, "File not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
      }

      FileUploadEntity file = fileOpt.get();
      response.put(SUCCESS_CONSTANT, true);
      response.put("file", file);
      response.put("processingStatus", fileUploadService.getProcessingStatus(fileId, userId));

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Error retrieving file details: {}", e.getMessage(), e);
      response.put(SUCCESS_CONSTANT, false);
      response.put(MESSAGE_CONSTANT, "Error retrieving file details");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /**
   * Delete a file and its associated data
   */
  @DeleteMapping("/{fileId}")
  public ResponseEntity<Map<String, Object>> deleteFile(
          @PathVariable Long fileId, @CurrentUser CustomUserDetails userDetails) {

    Map<String, Object> response = new HashMap<>();

    try {
      Long userId = userDetails.getId();
      fileUploadService.deleteFile(fileId, userId);

      response.put(SUCCESS_CONSTANT, true);
      response.put(MESSAGE_CONSTANT, "File deleted successfully");

      return ResponseEntity.ok(response);

    } catch (IllegalArgumentException e) {
      log.warn("File deletion error: {}", e.getMessage());
      response.put(SUCCESS_CONSTANT, false);
      response.put(MESSAGE_CONSTANT, e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

    } catch (Exception e) {
      log.error("Error deleting file: {}", e.getMessage(), e);
      response.put(SUCCESS_CONSTANT, false);
      response.put(MESSAGE_CONSTANT, "Error deleting file");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /**
   * Get user file statistics
   */
  @GetMapping("/stats")
  public ResponseEntity<Map<String, Object>> getFileStats(@CurrentUser CustomUserDetails userDetails) {
    Map<String, Object> response = new HashMap<>();

    try {
      Long userId = userDetails.getId();
      UserFileStats stats = fileUploadService.getUserFileStatistics(userId);

      response.put(SUCCESS_CONSTANT, true);
      response.put("stats", Map.of(
              "totalFiles", stats.totalFiles(),
              "totalRecordsProcessed", stats.totalRecordsProcessed(),
              "successfulUploads", stats.successfulUploads(),
              "failedUploads", stats.failedUploads(),
              "pendingUploads", stats.pendingUploads(),
              "successRate", stats.successRate(),
              "hasFailures", stats.hasFailures(),
              "hasUploads", stats.hasUploads()
      ));
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Error retrieving file stats: {}", e.getMessage(), e);
      response.put(SUCCESS_CONSTANT, false);
      response.put(MESSAGE_CONSTANT, "Error retrieving file stats");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

}
