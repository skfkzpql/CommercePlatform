package com.hyunn.commerceplatform.dto.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsersPasswordChangeRequestDto {

  @NotBlank
  private String currentPassword;

  @NotBlank
  @Size(min = 6, message = "New password must be at least 6 characters long")
  private String newPassword;

  @NotBlank
  @Size(min = 6, message = "New password must be at least 6 characters long")
  private String confirmPassword;
}