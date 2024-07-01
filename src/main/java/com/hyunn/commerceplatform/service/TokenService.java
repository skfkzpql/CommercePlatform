package com.hyunn.commerceplatform.service;

import com.hyunn.commerceplatform.dto.auth.JwtAuthenticationResponse;
import com.hyunn.commerceplatform.security.JwtTokenProvider.TokenType;
import org.springframework.security.core.Authentication;

public interface TokenService {

  void saveTokenToRedis(TokenType tokenType, String username, String token);

  String getTokenFromRedis(TokenType tokenType, String username);

  void removeTokenFromRedis(TokenType tokenType, String username);

  boolean validateTokenFromRedis(TokenType tokenType, String username, String token);

  boolean validateToken(String token);

  String getUsernameFromToken(String token);

  JwtAuthenticationResponse generateAuthTokens(Authentication authentication);

  String generateEmailToken(String username, String email, TokenType tokenType);

  boolean isTokenPresentInRedis(TokenType tokenType, String username);
}