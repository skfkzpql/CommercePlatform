package com.hyunn.commerceplatform.service;

import com.hyunn.commerceplatform.entity.Users;

public interface UserLockService {

  // 로그인 인증 관련 메서드
  boolean unlockWhenTimeExpired(Users user);

  void incrementFailedAttempts(Users user);

  void lockUser(Users user);

  // 유틸리티 메서드
  Users getUserOrThrow(String username);
}
