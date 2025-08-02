package com.salesdata.platform.fileupload.repository;

import com.salesdata.platform.fileupload.dto.FileUploadEntity;
import com.salesdata.platform.fileupload.dto.UserFileStats;
import com.salesdata.platform.fileupload.enums.UploadStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FileUploadRepository extends JpaRepository<FileUploadEntity, Long> {

  List<FileUploadEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

  Optional<FileUploadEntity> findByIdAndUserId(Long id, Long userId);

  List<FileUploadEntity> findByUploadStatus(UploadStatus uploadStatus);

  List<FileUploadEntity> findByUserIdAndUploadStatus(Long userId, UploadStatus uploadStatus);

  List<FileUploadEntity> findByUploadStatusOrderByCreatedAtAsc(UploadStatus uploadStatus);

  Long countByUserId(Long userId);

  Long countByUserIdAndUploadStatus(Long userId, UploadStatus uploadStatus);

  Boolean existsByIdAndUserId(Long id, Long userId);

  @Query(
      "SELECT COALESCE(SUM(f.recordsProcessed), 0) FROM FileUploadEntity f WHERE f.userId = :userId")
  Long getTotalRecordsProcessedByUserId(Long userId);

  @Query(
      """
      SELECT new com.salesdata.platform.fileupload.dto.UserFileStats(
          COUNT(f),
          CAST(COALESCE(SUM(f.recordsProcessed), 0) AS LONG),
          COALESCE(SUM(CASE WHEN f.uploadStatus = com.salesdata.platform.fileupload.enums.UploadStatus.COMPLETED THEN 1L ELSE 0L END), 0L),
          COALESCE(SUM(CASE WHEN f.uploadStatus = com.salesdata.platform.fileupload.enums.UploadStatus.FAILED THEN 1L ELSE 0L END), 0L)
      )
      FROM FileUploadEntity f
      WHERE f.userId = :userId
    """)
  UserFileStats getUserFileStats(@Param("userId") Long userId);

  List<FileUploadEntity> findByUploadStatusAndCreatedAtBefore(
      UploadStatus uploadStatus, LocalDateTime timeout);
}
