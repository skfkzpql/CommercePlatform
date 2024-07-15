package com.hyunn.commerceplatform.security;

import com.hyunn.commerceplatform.entity.Users;
import com.hyunn.commerceplatform.service.UserLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserLockService userLockService;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Users user = userLockService.getUserOrThrow(username);

    if (!userLockService.unlockWhenTimeExpired(user)) {
      throw new LockedException("User account is locked");
    }

    return new CustomUserDetails(user);
  }
}