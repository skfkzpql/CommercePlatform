package com.hyunn.commerceplatform.dto.auth;

import com.hyunn.commerceplatform.dto.users.UserTermAgreementDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class RegistrationRequestDto {

  @NotBlank
  @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters")
  private String username;

  @NotBlank
  @Size(min = 6, message = "Password must be at least 6 characters long")
  private String password;

  @NotBlank
  @Past(message = "Date of Birth should be in the past")
  private Date dateOfBirth;

  @NotBlank
  @Email(message = "Email should be valid")
  private String email;

  @NotNull
  private List<UserTermAgreementDto> termAgreements;
}
