package com.hyunn.commerceplatform.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleException(Exception e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
  }

  @ExceptionHandler(UserException.class)
  public ResponseEntity<String> handleUserException(UserException e) {
    return ResponseEntity.status(e.getStatus()).body(e.getMessage());
  }

  @ExceptionHandler(TokenException.class)
  public ResponseEntity<String> handleJwtException(TokenException e) {
    return ResponseEntity.status(e.getStatus()).body(e.getMessage());
  }

}
