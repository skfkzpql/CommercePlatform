package com.hyunn.commerceplatform.service;

import com.hyunn.commerceplatform.dto.auth.LoginRequestDto;
import com.hyunn.commerceplatform.dto.auth.PasswordResetEmailRequestDto;
import com.hyunn.commerceplatform.dto.auth.RegistrationRequestDto;
import com.hyunn.commerceplatform.dto.auth.ResetPasswordRequestDto;
import com.hyunn.commerceplatform.dto.users.UsersDetailResponseDto;
import com.hyunn.commerceplatform.dto.users.UsersEmailUpdateRequestDto;
import com.hyunn.commerceplatform.dto.users.UsersPasswordChangeRequestDto;

public interface UserService {

  void registerUser(RegistrationRequestDto registrationRequest);

  void loginUser(LoginRequestDto loginRequest);

  UsersDetailResponseDto getUserByUsername(String username);

  void deleteUser(String username);

  void updateEmail(String username, UsersEmailUpdateRequestDto usersEmailUpdateRequestDto);

  void updatePassword(String username, UsersPasswordChangeRequestDto usersPasswordChangeRequestDto);

  void sendPasswordResetLink(PasswordResetEmailRequestDto passwordResetRequestDto);

  void resetPassword(ResetPasswordRequestDto resetPasswordRequestDto);

  void verifyEmail(String email);
}
