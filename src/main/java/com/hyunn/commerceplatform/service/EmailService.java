package com.hyunn.commerceplatform.service;

public interface EmailService {

  void sendVerificationEmail(String to, String token);

  void sendPasswordResetEmail(String to, String token);
}