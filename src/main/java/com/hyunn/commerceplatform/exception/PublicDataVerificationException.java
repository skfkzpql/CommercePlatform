package com.hyunn.commerceplatform.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PublicDataVerificationException extends RuntimeException {

  private final HttpStatus status;

  private PublicDataVerificationException(String message, HttpStatus status) {
    super(message);
    this.status = status;
  }

  public static PublicDataVerificationException apiCallFailed(String statusCode) {
    return new PublicDataVerificationException("API 호출 실패: " + statusCode,
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  public static PublicDataVerificationException invalidResponseStatus(String statusCode) {
    return new PublicDataVerificationException("API 응답 상태 코드 오류: " + statusCode,
        HttpStatus.BAD_GATEWAY);
  }

  public static PublicDataVerificationException responseParsingFailed(String message) {
    return new PublicDataVerificationException("Response parsing 실패: " + message,
        HttpStatus.INTERNAL_SERVER_ERROR);
  }
}