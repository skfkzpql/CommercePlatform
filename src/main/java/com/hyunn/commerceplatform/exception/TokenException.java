package com.hyunn.commerceplatform.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TokenException extends RuntimeException {

  private final HttpStatus status;

  private TokenException(String message, HttpStatus status) {
    super(message);
    this.status = status;
  }

  public static TokenException invalidToken() {
    return new TokenException("Invalid token", HttpStatus.BAD_REQUEST);
  }

}