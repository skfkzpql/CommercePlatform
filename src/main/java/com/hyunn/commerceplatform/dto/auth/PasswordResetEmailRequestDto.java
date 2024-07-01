package com.hyunn.commerceplatform.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetEmailRequestDto {

  @NotBlank
  private String username;

  @NotBlank
  @Email(message = "Email should be valid")
  private String email;
}
