package com.salesdata.platform.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sales_data")
public class SalesData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    @NotNull(message = "User ID is required")
    private Integer userId;

    @Column(name = "file_upload_id", nullable = false)
    @NotNull(message = "File upload ID is required")
    private Integer fileUploadId;

    @Column(name = "product_name", nullable = false)
    @NotBlank(message = "Product name is required")
    private String productName;

    @Column(name = "product_price", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Product price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Product price must be greater or equal to 0")
    private BigDecimal productPrice;

    @Column(name = "sale_location")
    private String saleLocation;

    @Column(name = "sale_date", nullable = false)
    @NotNull(message = "Sale date is required")
    private Integer saleDate;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

}
