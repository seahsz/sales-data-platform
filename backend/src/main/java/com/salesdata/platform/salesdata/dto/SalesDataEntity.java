package com.salesdata.platform.salesdata.dto;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sales_data")
public class SalesDataEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "file_upload_id", nullable = false)
  private Long fileUploadId;

  @Column(name = "product_name", nullable = false)
  private String productName;

  @Column(name = "product_price", precision = 10, scale = 2, nullable = false)
  private BigDecimal productPrice;

  @Column(name = "sale_location")
  private String saleLocation;

  @Column(name = "sale_date", nullable = false)
  private LocalDate saleDate;

  @Column(nullable = false)
  private Integer quantity;

  @Column(name = "total_amount", precision = 10, scale = 2)
  private BigDecimal totalAmount;

  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  @PrePersist
  @PreUpdate
  private void calculateTotalAmount() {
    if (productPrice != null && quantity != null) {
      this.totalAmount = productPrice.multiply(BigDecimal.valueOf(quantity));
    }
  }
}
