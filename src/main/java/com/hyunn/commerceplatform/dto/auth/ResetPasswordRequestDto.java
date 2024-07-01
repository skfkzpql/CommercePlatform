package com.hyunn.commerceplatform.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequestDto {

  @NotBlank
  private String token;

  @NotBlank
  @Size(min = 6, message = "New password must be at least 6 characters long")
  private String newPassword;

  @NotBlank
  @Size(min = 6, message = "New password must be at least 6 characters long")
  private String confirmPassword;
}
