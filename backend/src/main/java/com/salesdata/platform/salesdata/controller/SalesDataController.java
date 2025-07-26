package com.salesdata.platform.salesdata.controller;

import com.salesdata.platform.auth.service.UserService;
import com.salesdata.platform.entity.UserEntity;
import com.salesdata.platform.salesdata.dto.CreateSalesDataRequest;
import com.salesdata.platform.salesdata.dto.SalesDataEntity;
import com.salesdata.platform.salesdata.dto.SalesDataSummary;
import com.salesdata.platform.salesdata.service.SalesDataService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SalesDataController {

  private final SalesDataService salesDataService;
  private final UserService userService;

  private static final String MESSAGE_CONSTANT = "message";
  private static final String ERROR_CONSTANT = "error";
  private static final String SUCCESS_CONSTANT = "success";

  private Long getCurrentUserId() {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication != null
          && authentication.getPrincipal() instanceof UserDetails userDetails) {
        String username = userDetails.getUsername();

        // Get user from database using username
        UserEntity user = userService.findUserByUsername(username);
        if (user != null) {
          return user.getId();
        }
      }
      throw new RuntimeException("No authenticated user found");
    } catch (Exception e) {
      throw new RuntimeException("Failed to get current user: " + e.getMessage());
    }
  }

  @PostMapping("/create")
  public ResponseEntity<Map<String, Object>> createSalesData(
      @Valid @RequestBody CreateSalesDataRequest request) {
    try {
      Long userId = getCurrentUserId();

      // Convert DTO to Entity
      SalesDataEntity salesDataEntity = request.toSalesDataEntity(userId);

      salesDataService.createSalesRecord(salesDataEntity, userId);

      Map<String, Object> response = new HashMap<>();
      response.put(MESSAGE_CONSTANT, "Sales record created successfully");
      response.put("data", salesDataEntity);
      response.put(SUCCESS_CONSTANT, true);

      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (IllegalArgumentException e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put(MESSAGE_CONSTANT, "Validation error");
      errorResponse.put(ERROR_CONSTANT, e.getMessage());
      errorResponse.put(SUCCESS_CONSTANT, false);

      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put(MESSAGE_CONSTANT, "Failed to create sales record");
      errorResponse.put(ERROR_CONSTANT, e.getMessage());
      errorResponse.put(SUCCESS_CONSTANT, false);

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> getAllSalesRecord() {
    try {
      Long userId = getCurrentUserId();
      List<SalesDataEntity> salesRecords = salesDataService.getAllSalesRecords(userId);

      Map<String, Object> response = new HashMap<>();
      response.put(MESSAGE_CONSTANT, "Sales records found successfully");
      response.put("data", salesRecords);
      response.put("count", salesRecords.size());
      response.put(SUCCESS_CONSTANT, true);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put(MESSAGE_CONSTANT, "Failed to get all sales records");
      errorResponse.put(ERROR_CONSTANT, e.getMessage());
      errorResponse.put(SUCCESS_CONSTANT, false);

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  /**
   * READ - Get specific sales record by ID
   *
   * @param id
   * @return ResponseEntity<Map<String, Object>>
   */
  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getSalesRecordById(@PathVariable Long id) {

    try {
      Long userId = getCurrentUserId();
      Optional<SalesDataEntity> salesRecord = salesDataService.getSalesRecordById(id, userId);

      if (salesRecord.isPresent()) {
        Map<String, Object> response = new HashMap<>();
        response.put(MESSAGE_CONSTANT, "Sales record found successfully");
        response.put("data", salesRecord.get());
        response.put(SUCCESS_CONSTANT, true);

        return ResponseEntity.ok(response);
      } else {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put(MESSAGE_CONSTANT, "Sales record not found");
        errorResponse.put(SUCCESS_CONSTANT, false);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
      }
    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put(MESSAGE_CONSTANT, "Error getting sales record");
      errorResponse.put(ERROR_CONSTANT, e.getMessage());
      errorResponse.put(SUCCESS_CONSTANT, false);

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  /** Delete sales record */
  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, Object>> deleteSalesRecord(@PathVariable Long id) {
    try {
      Long userId = getCurrentUserId();

      if (salesDataService.deleteSalesRecordById(id, userId)) {

        Map<String, Object> response = new HashMap<>();
        response.put(MESSAGE_CONSTANT, "Sales record deleted successfully");
        response.put(SUCCESS_CONSTANT, true);

        return ResponseEntity.ok(response);
      } else {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put(MESSAGE_CONSTANT, "Sales record not found");
        errorResponse.put(SUCCESS_CONSTANT, false);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
      }

    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put(MESSAGE_CONSTANT, "Error deleting sales record");
      errorResponse.put(ERROR_CONSTANT, e.getMessage());
      errorResponse.put(SUCCESS_CONSTANT, false);

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  /** Get sales summary for current user GET /api/sales/summary */
  @GetMapping("/summary")
  public ResponseEntity<Map<String, Object>> getSalesRecordSummary() {
    try {
      Long userId = getCurrentUserId();
      SalesDataSummary summary = salesDataService.getSalesDataSummary(userId);

      Map<String, Object> response = parseSummaryDataAsMap(summary);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put(MESSAGE_CONSTANT, "Error getting sales record summary");
      errorResponse.put(ERROR_CONSTANT, e.getMessage());
      errorResponse.put(SUCCESS_CONSTANT, false);

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  private static Map<String, Object> parseSummaryDataAsMap(SalesDataSummary summary) {
    Map<String, Object> summaryData = new HashMap<>();
    summaryData.put("totalRecords", summary.getTotalRecords());
    summaryData.put("totalSalesAmount", summary.getTotalAmount());
    summaryData.put("averageAmount", summary.getAverageAmount());
    summaryData.put("totalQuantity", summary.getTotalQuantity());
    summaryData.put("averagePrice", summary.getAveragePrice());

    Map<String, Object> response = new HashMap<>();
    response.put(MESSAGE_CONSTANT, "Sales summary retrieved successfully");
    response.put("data", summaryData);
    response.put(SUCCESS_CONSTANT, true);
    return response;
  }

  /** HEALTH CHECK - Test endpoint to verify controller is working GET /api/sales/health */
  @GetMapping("/health")
  public ResponseEntity<Map<String, Object>> healthCheck() {
    try {
      Long userId = getCurrentUserId();

      Map<String, Object> response = new HashMap<>();
      response.put(MESSAGE_CONSTANT, "Sales controller - Healthy");
      response.put("userId", userId);
      response.put("timestamp", System.currentTimeMillis());
      response.put(SUCCESS_CONSTANT, true);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put(MESSAGE_CONSTANT, "Sales controller - Unhealthy");
      errorResponse.put(ERROR_CONSTANT, e.getMessage());
      errorResponse.put(SUCCESS_CONSTANT, false);

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }
}
