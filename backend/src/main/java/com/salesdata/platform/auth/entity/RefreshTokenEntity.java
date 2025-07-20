package com.salesdata.platform.auth.entity;

import com.salesdata.platform.entity.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "refresh_tokens")
public class RefreshTokenEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String token;

  @Column(name = "expiry_date", nullable = false)
  private LocalDateTime expiryDate;

  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private UserEntity user;

  @Column(name = "is_revoked")
  private Boolean isRevoked = false;

  public RefreshTokenEntity(String token, LocalDateTime expiryDate, UserEntity user) {
    this.token = token;
    this.expiryDate = expiryDate;
    this.user = user;
    this.createdAt = LocalDateTime.now();
    this.isRevoked = false;
  }
}
