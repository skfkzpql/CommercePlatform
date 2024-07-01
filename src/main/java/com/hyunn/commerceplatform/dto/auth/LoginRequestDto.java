package com.hyunn.commerceplatform.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequestDto {

  @NotBlank
  @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters")
  private String username;

  @NotBlank
  @Size(min = 6, message = "Password must be at least 6 characters long")
  private String password;
}
