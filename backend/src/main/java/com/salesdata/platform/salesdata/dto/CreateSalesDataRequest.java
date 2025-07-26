package com.salesdata.platform.salesdata.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CreateSalesDataRequest {

  @NotNull(message = "File upload ID is required")
  private Long fileUploadId;

  @NotBlank(message = "Product name is required")
  @Size(max = 255, message = "Product name must not exceed 255 characters")
  private String productName;

  @NotNull(message = "Product price is required")
  @DecimalMin(value = "0.01", message = "Product price must be greater than 0")
  @Digits(integer = 8, fraction = 2, message = "Invalid price format")
  private BigDecimal productPrice;

  @NotNull(message = "Quantity is required")
  @Min(value = 1, message = "Quantity must be at least 1")
  private Integer quantity;

  @NotNull(message = "Sale date is required")
  @PastOrPresent(message = "Sale date cannot be in the future")
  private LocalDate saleDate;

  @Size(max = 255, message = "Sale location must not exceed 255 characters")
  private String saleLocation;

  /**
   * Convert this DTO to SalesDataEntity
   *
   * @param userId The user ID from JWT token
   * @return Populated SalesDataEntity ready for persistence
   */
  public SalesDataEntity toSalesDataEntity(Long userId) {
    SalesDataEntity entity = new SalesDataEntity();
    entity.setUserId(userId);
    entity.setProductName(productName);
    entity.setProductPrice(productPrice);
    entity.setQuantity(quantity);
    entity.setSaleDate(saleDate);
    entity.setSaleLocation(saleLocation);
    entity.setFileUploadId(fileUploadId);
    return entity;
  }
}
