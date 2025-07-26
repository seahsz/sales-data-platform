package com.salesdata.platform.salesdata.service;

import com.salesdata.platform.salesdata.dto.SalesDataEntity;
import com.salesdata.platform.salesdata.dto.SalesDataSummary;
import com.salesdata.platform.salesdata.repository.SalesDataRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SalesDataService {

  private final SalesDataRepository salesDataRepository;

  // CREATE
  public SalesDataEntity createSalesRecord(SalesDataEntity salesDataEntity, Long userId) {
    salesDataEntity.setUserId(userId);
    validateSalesData(salesDataEntity);

    return salesDataRepository.save(salesDataEntity);
  }

  // READ - Get all for user
  @Transactional(readOnly = true)
  public List<SalesDataEntity> getAllSalesRecords(Long userId) {
    return salesDataRepository.findByUserIdOrderBySaleDateDesc(userId);
  }

  @Transactional(readOnly = true)
  public Optional<SalesDataEntity> getSalesRecordById(Long recordId, Long userId) {
    return salesDataRepository.findByIdAndUserId(recordId, userId);
  }

  @Transactional(readOnly = true)
  public SalesDataSummary getSalesDataSummary(Long userId) {
    long totalRecords = salesDataRepository.countByUserId(userId);
    BigDecimal totalAmount = salesDataRepository.getTotalSalesAmountByUserId(userId);
    BigDecimal averagePrice = salesDataRepository.getAveragePriceByUserId(userId);

    return SalesDataSummary.builder()
        .totalRecords(totalRecords)
        .totalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO)
        .averageAmount(SalesDataSummary.calculateAverage(totalAmount, totalRecords))
        .averagePrice(averagePrice)
        .build();
  }

  // DELETE
  public boolean deleteSalesRecordById(Long recordId, Long userId) {
    Optional<SalesDataEntity> existingRecord =
        salesDataRepository.findByIdAndUserId(recordId, userId);

    if (existingRecord.isPresent()) {
      salesDataRepository.deleteById(recordId);
      return true;
    }

    return false;
  }

  // BUSINESS VALIDATION
  private void validateSalesData(SalesDataEntity salesDataEntity) {
    if (salesDataEntity.getProductPrice().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Product price must be greater than zero");
    }

    if (salesDataEntity.getQuantity() <= 0) {
      throw new IllegalArgumentException("Quantity must be greater than zero");
    }
  }
}
