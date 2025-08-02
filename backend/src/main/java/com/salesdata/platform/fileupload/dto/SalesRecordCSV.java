package com.salesdata.platform.fileupload.dto;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.CsvDates;
import com.salesdata.platform.salesdata.dto.SalesDataEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SalesRecordCSV {

  @CsvBindByName(column = "product_name", required = true)
  private String productName;

  @CsvBindByName(column = "product_price", required = true)
  private BigDecimal productPrice;

  @CsvBindByName(column = "quantity", required = true)
  private Integer quantity;

  @CsvBindByName(column = "sale_date", required = true)
  @CsvDates(
      value = {
        @CsvDate(value = "yyyy-MM-dd"),
        @CsvDate(value = "MM/dd/yyyy"),
        @CsvDate(value = "dd/MM/yyyy"),
        @CsvDate(value = "yyyy/MM/dd"),
        @CsvDate(value = "dd-MM-yyyy"),
        @CsvDate(value = "MM-dd-yyyy")
      })
  private LocalDate saleDate;

  @CsvBindByName(column = "sale_location", required = false)
  private String saleLocation;

  // Validation method
  public void validate() throws IllegalArgumentException {
    if (productName == null || productName.trim().isEmpty()) {
      throw new IllegalArgumentException("Product name cannot be empty");
    }
    if (productName.length() > 255) {
      throw new IllegalArgumentException("Product name cannot be longer than 255 characters");
    }
    if (productPrice == null || productPrice.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Product price must be greater than 0");
    }
    if (productPrice.compareTo(new BigDecimal("99999999.99")) > 0) {
      throw new IllegalArgumentException("Product price too large (max 99,999,999.99)");
    }
    if (quantity == null || quantity <= 0) {
      throw new IllegalArgumentException("Quantity must be greater than 0");
    }
    if (quantity > 1000000) {
      throw new IllegalArgumentException("Quantity too large (max 1,000,000)");
    }
    if (saleDate == null) {
      throw new IllegalArgumentException("Sale date is required");
    }
    if (saleDate.isAfter(LocalDate.now())) {
      throw new IllegalArgumentException("Sale date cannot be in the future");
    }
    if (saleLocation != null && saleLocation.length() > 255) {
      throw new IllegalArgumentException("Sale location cannot be longer than 255 characters");
    }
  }

  public SalesDataEntity toSalesDataEntity(Long userId, Long fileUploadId) {
    SalesDataEntity entity = new SalesDataEntity();
    entity.setUserId(userId);
    entity.setFileUploadId(fileUploadId);
    entity.setProductName(productName.trim());
    entity.setProductPrice(productPrice);
    entity.setQuantity(quantity);
    entity.setSaleDate(saleDate);
    entity.setSaleLocation(
        saleLocation != null && !saleLocation.trim().isEmpty() ? saleLocation.trim() : null);
    return entity;
  }
}
