package com.hyunn.commerceplatform.service.impl;

import com.hyunn.commerceplatform.dto.auth.JwtAuthenticationResponse;
import com.hyunn.commerceplatform.security.JwtTokenProvider;
import com.hyunn.commerceplatform.security.JwtTokenProvider.TokenType;
import com.hyunn.commerceplatform.service.TokenService;
import io.jsonwebtoken.Claims;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class TokenServiceImpl implements TokenService {

  private final RedisTemplate<String, String> redisTemplate;
  private final JwtTokenProvider jwtTokenProvider;

  @Autowired
  public TokenServiceImpl(RedisTemplate<String, String> redisTemplate,
      JwtTokenProvider jwtTokenProvider) {
    this.redisTemplate = redisTemplate;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  public void saveTokenToRedis(TokenType tokenType, String username, String token) {
    Claims claims = jwtTokenProvider.getClaimsFromToken(token);
    long expirationTime = claims.getExpiration().getTime();
    long currentTime = System.currentTimeMillis();
    long remainingTimeInSeconds = (expirationTime - currentTime) / 1000;

    redisTemplate.opsForValue().set(
        tokenType + username,
        token,
        remainingTimeInSeconds,
        TimeUnit.SECONDS
    );
  }

  @Override
  public String getTokenFromRedis(TokenType tokenType, String username) {
    return redisTemplate.opsForValue().get(tokenType + username);
  }

  @Override
  public void removeTokenFromRedis(TokenType tokenType, String username) {
    redisTemplate.delete(tokenType + username);
  }

  @Override
  public boolean validateTokenFromRedis(TokenType tokenType, String username, String token) {
    String storedToken = getTokenFromRedis(tokenType, username);
    return storedToken != null && storedToken.equals(token) && validateToken(token);
  }

  @Override
  public boolean validateToken(String token) {
    return jwtTokenProvider.validateToken(token);
  }

  @Override
  public String getUsernameFromToken(String token) {
    return jwtTokenProvider.getUsernameFromToken(token);
  }

  @Override
  public JwtAuthenticationResponse generateAuthTokens(Authentication authentication) {
    String username = authentication.getName();

    String accessToken = jwtTokenProvider.generateAuthToken(authentication, TokenType.ACCESS);
    String refreshToken = jwtTokenProvider.generateAuthToken(authentication, TokenType.REFRESH);

    saveTokenToRedis(TokenType.ACCESS, username, accessToken);
    saveTokenToRedis(TokenType.REFRESH, username, refreshToken);

    return new JwtAuthenticationResponse(accessToken, refreshToken);
  }

  @Override
  public boolean isTokenPresentInRedis(TokenType tokenType, String username) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(tokenType + username));
  }
}