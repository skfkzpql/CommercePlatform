package com.hyunn.commerceplatform.repository;

import com.hyunn.commerceplatform.entity.LoginLog;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {

  @Modifying
  @Query("DELETE FROM LoginLog l WHERE l.loginTime < :cutoffDate")
  int deleteOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
}