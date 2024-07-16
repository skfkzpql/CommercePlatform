package com.hyunn.commerceplatform.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

  private final HttpStatus status;

  private BusinessException(String message, HttpStatus status) {
    super(message);
    this.status = status;
  }

  public static BusinessException businessNotFound() {
    return new BusinessException("Business not found", HttpStatus.NOT_FOUND);
  }

  public static BusinessException businessAlreadyExists() {
    return new BusinessException("Business already exists", HttpStatus.CONFLICT);
  }

  public static BusinessException invalidBusinessInfo() {
    return new BusinessException("Invalid business information", HttpStatus.BAD_REQUEST);
  }

  public static BusinessException unauthorized() {
    return new BusinessException("Unauthorized to perform this action", HttpStatus.FORBIDDEN);
  }

  public static BusinessException usernameMismatch() {
    return new BusinessException("Username and representative name do not match",
        HttpStatus.BAD_REQUEST);
  }
}