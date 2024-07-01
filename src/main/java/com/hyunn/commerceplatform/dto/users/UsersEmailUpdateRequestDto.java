package com.hyunn.commerceplatform.dto.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsersEmailUpdateRequestDto {

  @NotBlank
  @Email
  private String newEmail;
}
