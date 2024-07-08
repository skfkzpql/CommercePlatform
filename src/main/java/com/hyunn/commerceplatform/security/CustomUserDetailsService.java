package com.hyunn.commerceplatform.security;

import com.hyunn.commerceplatform.entity.Users;
import com.hyunn.commerceplatform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserService userService;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Users user = userService.getUserOrThrow(username);

    if (!user.isAccountNonLocked()) {
      if (userService.unlockWhenTimeExpired(user)) {
        // 계정 잠금이 해제되었다면, 업데이트된 사용자 정보를 반환
        return new CustomUserDetails(user);
      } else {
        // 계정이 여전히 잠겨있다면, LockedException을 던집니다.
        throw new LockedException("User account is locked");
      }
    }

    return new CustomUserDetails(user);
  }
}