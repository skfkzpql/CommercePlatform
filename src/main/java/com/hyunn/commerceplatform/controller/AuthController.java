package com.hyunn.commerceplatform.controller;

import com.hyunn.commerceplatform.dto.auth.JwtAuthenticationResponse;
import com.hyunn.commerceplatform.dto.auth.LoginRequestDto;
import com.hyunn.commerceplatform.dto.auth.PasswordResetEmailRequestDto;
import com.hyunn.commerceplatform.dto.auth.RegistrationRequestDto;
import com.hyunn.commerceplatform.dto.auth.ResetPasswordRequestDto;
import com.hyunn.commerceplatform.security.JwtTokenProvider.TokenType;
import com.hyunn.commerceplatform.service.TokenService;
import com.hyunn.commerceplatform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserService userService;
  private final TokenService tokenService;

  @Autowired
  public AuthController(UserService userService, TokenService tokenService) {
    this.userService = userService;
    this.tokenService = tokenService;
  }

  @PostMapping("/register")
  public ResponseEntity<?> registerUser(
      @Valid @RequestBody RegistrationRequestDto registrationRequest) {
    userService.registerUser(registrationRequest);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/login")
  public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequestDto loginRequest) {
    Authentication authentication = userService.loginUser(loginRequest);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    JwtAuthenticationResponse tokens = tokenService.generateAuthTokens(authentication);
    return ResponseEntity.status(HttpStatus.OK).body(tokens);
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logoutUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    tokenService.removeTokenFromRedis(TokenType.REFRESH, authentication.getName());
    SecurityContextHolder.clearContext();
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PostMapping("/refresh")
  public ResponseEntity<?> refreshToken() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    JwtAuthenticationResponse newTokens = tokenService.generateAuthTokens(authentication);
    return ResponseEntity.status(HttpStatus.OK).body(newTokens);
  }

  @PostMapping("/send-password-reset-link")
  public ResponseEntity<?> sendPasswordResetLink(
      @Valid @RequestBody PasswordResetEmailRequestDto passwordResetRequest) {
    userService.sendPasswordResetLink(passwordResetRequest);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PostMapping("/reset-password")
  public ResponseEntity<?> resetPassword(
      @Valid @RequestBody ResetPasswordRequestDto resetPasswordRequest) {
    userService.resetPassword(resetPasswordRequest);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PostMapping("/verify-email")
  public ResponseEntity<?> verifyEmail(@RequestBody String token) {
    userService.verifyEmail(token);
    return ResponseEntity.status(HttpStatus.OK).build();
  }
}