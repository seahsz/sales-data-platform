package com.salesdata.platform.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "file_uploads")
public class FileUploadEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  @NotNull(message = "UserEntity ID is required")
  private Long userId;

  @NotBlank(message = "Filename is required")
  private String fileName;

  @Column(name = "upload_date")
  private LocalDateTime uploadDate = LocalDateTime.now();

  @Column(name = "row_count")
  private Integer rowCount = 0;

  private String status = "pending";
}
