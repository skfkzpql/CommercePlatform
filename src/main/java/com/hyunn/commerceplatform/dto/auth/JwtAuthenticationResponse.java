package com.hyunn.commerceplatform.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtAuthenticationResponse {

  String accessToken;
  String refreshToken;
}
