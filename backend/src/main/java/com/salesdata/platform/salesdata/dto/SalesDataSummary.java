package com.salesdata.platform.salesdata.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SalesDataSummary {
  private long totalRecords;
  private BigDecimal totalAmount;
  private BigDecimal averageAmount;
  private long totalQuantity;
  private BigDecimal averagePrice;

  public static BigDecimal calculateAverage(BigDecimal totalAmount, long count) {
    if (count > 0 && totalAmount != null && totalAmount.compareTo(BigDecimal.ZERO) > 0) {
      return totalAmount.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }
    return BigDecimal.ZERO;
  }
}
