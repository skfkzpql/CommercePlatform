package com.hyunn.commerceplatform.security;

import com.hyunn.commerceplatform.entity.Users;
import com.hyunn.commerceplatform.exception.UserException;
import com.hyunn.commerceplatform.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UsersRepository usersRepository;

  @Override
  public UserDetails loadUserByUsername(String username) {
    Users user = usersRepository.findByUsername(username)
        .orElseThrow(UserException::userNotFound);
    return new CustomUserDetails(user);
  }
}