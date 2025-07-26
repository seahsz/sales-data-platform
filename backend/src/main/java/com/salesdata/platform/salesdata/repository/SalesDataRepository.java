package com.salesdata.platform.salesdata.repository;

import com.salesdata.platform.salesdata.dto.SalesDataEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesDataRepository extends JpaRepository<SalesDataEntity, Long> {

  /** Find all sales records for a specific user */
  List<SalesDataEntity> findByUserIdOrderBySaleDateDesc(Long userId);

  /** Find a specific sales record by ID and user (security check) */
  Optional<SalesDataEntity> findByIdAndUserId(Long id, Long userId);

  /** Count total records for a user */
  long countByUserId(Long userId);

  // FILTERING AND SEARCH QUERIES

  /** Find sales by date range for a user */
  @Query(
      "SELECT s FROM SalesDataEntity s WHERE s.userId = :userId "
          + "AND s.saleDate BETWEEN :startDate AND :endDate "
          + "ORDER BY s.saleDate DESC")
  List<SalesDataEntity> findByUserIdAndDateRange(
      @Param("userId") Long userId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  // ANALYTICS QUERIES
  /** Calculate total sales amount for a user */
  @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM SalesDataEntity s WHERE s.userId = :userId")
  BigDecimal getTotalSalesAmountByUserId(@Param("userId") Long userId);

  /** Calculate total quantity sold for a user */
  @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM SalesDataEntity s WHERE s.userId = :userId")
  BigDecimal getTotalQuantityByUserId(@Param("userId") Long userId);

  /** Calculate total quantity sold for a user */
  @Query("SELECT COALESCE(AVG(s.productPrice), 0) FROM SalesDataEntity s WHERE s.userId = :userId")
  BigDecimal getAveragePriceByUserId(@Param("userId") Long userId);

  // BATCH OPERATIONS

  /** Delete all sales records for a specific file upload */
  void deleteByFileUploadId(Long fileUploadId);

  /** Find all sales records from a specific file upload */
  List<SalesDataEntity> findByFileUploadIdOrderBySaleDateDesc(Long fileUploadId);

  /** Check if user has any sales records */
  boolean existsByUserId(Long userId);
}
