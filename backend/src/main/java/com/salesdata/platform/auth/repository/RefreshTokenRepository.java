package com.salesdata.platform.auth.repository;

import com.salesdata.platform.auth.entity.RefreshTokenEntity;
import com.salesdata.platform.entity.UserEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

  Optional<RefreshTokenEntity> findByToken(String token);

  void deleteByUser(UserEntity userEntity);

  void deleteByExpiryDateBefore(LocalDateTime dateTime);

  @Modifying
  @Query("UPDATE RefreshTokenEntity rt SET rt.isRevoked = true WHERE rt.user = ?1")
  void revokeAllUserTokens(UserEntity userEntity);
}
