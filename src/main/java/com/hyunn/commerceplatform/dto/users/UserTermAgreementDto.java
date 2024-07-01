package com.hyunn.commerceplatform.dto.users;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserTermAgreementDto {

  @NotNull
  private Long termId;

  @NotNull
  private Boolean agreed;
}