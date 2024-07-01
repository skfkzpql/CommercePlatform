package com.hyunn.commerceplatform.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserException extends RuntimeException {

  private final HttpStatus status;

  private UserException(String message, HttpStatus status) {
    super(message);
    this.status = status;
  }

  public static UserException userNotFound() {
    return new UserException("User not found", HttpStatus.NOT_FOUND);
  }

  public static UserException usernameAlreadyExists() {
    return new UserException("Username already exists", HttpStatus.CONFLICT);
  }

  public static UserException emailAlreadyExists() {
    return new UserException("Email already exists", HttpStatus.CONFLICT);
  }

  public static UserException invalidCredentials() {
    return new UserException("Invalid username or password", HttpStatus.UNAUTHORIZED);
  }

  public static UserException emailNotVerified() {
    return new UserException("Email not verified", HttpStatus.FORBIDDEN);
  }

  public static UserException invalidToken() {
    return new UserException("Invalid or expired token", HttpStatus.UNAUTHORIZED);
  }

  public static UserException passwordConfirmMismatch() {
    return new UserException("New password and confirm password do not match",
        HttpStatus.BAD_REQUEST);
  }

  public static UserException invalidCurrentPassword() {
    return new UserException("Current password is incorrect", HttpStatus.BAD_REQUEST);
  }

  public static UserException emailMismatch() {
    return new UserException("Email does not match with the user's email", HttpStatus.BAD_REQUEST);
  }

  public static UserException mandatoryTermsNotAgreed() {
    return new UserException("All mandatory terms must be agreed", HttpStatus.BAD_REQUEST);
  }

  public static UserException invalidTermId(Long termId) {
    return new UserException("Invalid term ID: " + termId, HttpStatus.NOT_FOUND);
  }

}