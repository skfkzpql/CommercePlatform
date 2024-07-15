package com.hyunn.commerceplatform.service.impl;

import com.hyunn.commerceplatform.entity.Users;
import com.hyunn.commerceplatform.exception.UserException;
import com.hyunn.commerceplatform.repository.UsersRepository;
import com.hyunn.commerceplatform.service.UserLockService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserLockServiceImpl implements UserLockService {

  private final UsersRepository userRepository;

  @Value("${app.login.lock-duration-minutes}")
  private int lockDurationMinutes;

  @Value("${app.login.max-fail-attempts}")
  private int maxFailAttempts;

  // 로그인 인증 관련 메서드
  @Override
  public boolean unlockWhenTimeExpired(Users user) {
    if (user.isAccountNonLocked()) {
      return true;
    } else {
      LocalDateTime lockTime = user.getLockTime();
      if (LocalDateTime.now().isAfter(lockTime.plusMinutes(lockDurationMinutes))) {
        user.setFailedAttempt(0);
        user.setAccountNonLocked(true);
        userRepository.save(user);
        return true;
      } else {
        return false;
      }
    }
  }

  @Override
  public void incrementFailedAttempts(Users user) {
    int failedAttempts = user.getFailedAttempt() + 1;
    user.setFailedAttempt(failedAttempts);
    if (failedAttempts >= maxFailAttempts) {
      lockUser(user);
    } else {
      userRepository.save(user);
    }
  }

  @Override
  public void lockUser(Users user) {
    user.setLockTime(LocalDateTime.now());
    user.setAccountNonLocked(false);
    userRepository.save(user);
  }

  // 유틸리티 메서드
  @Override
  public Users getUserOrThrow(String username) {
    return userRepository.findByUsername(username)
        .orElseThrow(UserException::userNotFound);
  }
}
