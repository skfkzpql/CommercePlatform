package com.hyunn.commerceplatform.service;

import com.hyunn.commerceplatform.dto.auth.PasswordResetEmailRequestDto;
import com.hyunn.commerceplatform.dto.auth.RegistrationRequestDto;
import com.hyunn.commerceplatform.dto.auth.ResetPasswordRequestDto;
import com.hyunn.commerceplatform.dto.users.UsersDetailResponseDto;
import com.hyunn.commerceplatform.dto.users.UsersEmailUpdateRequestDto;
import com.hyunn.commerceplatform.dto.users.UsersPasswordChangeRequestDto;
import com.hyunn.commerceplatform.entity.Users;
import java.util.concurrent.CompletableFuture;

public interface UserService {

  void registerUser(RegistrationRequestDto requestDto);

  UsersDetailResponseDto getUserByUsername(String username);

  void deleteUser(String username);

  void updateEmail(String username, UsersEmailUpdateRequestDto dto);

  void updatePassword(String username, UsersPasswordChangeRequestDto dto);

  CompletableFuture<?> sendPasswordResetLink(PasswordResetEmailRequestDto dto);

  void resetPassword(ResetPasswordRequestDto dto);

  void verifyEmail(String token);

  Users getUserOrThrow(String username);

  boolean unlockWhenTimeExpired(Users user);

  void incrementFailedAttempts(Users user);

  void lockUser(Users user);
}