package com.hyunn.commerceplatform.service.impl;

import com.hyunn.commerceplatform.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

  private final JavaMailSender emailSender;

  @Value("${app.email.verification-url}")
  private String verificationUrl;

  @Value("${app.email.password-reset-url}")
  private String passwordResetUrl;

  @Override
  public void sendVerificationEmail(String to, String token) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setSubject("이메일 인증");
    message.setText("다음 링크를 클릭하여 이메일을 인증해주세요: "
        + verificationUrl + "?token=" + token);
    emailSender.send(message);
  }

  @Override
  public void sendPasswordResetEmail(String to, String token) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setSubject("비밀번호 재설정");
    message.setText("다음 링크를 클릭하여 비밀번호를 재설정해주세요: "
        + passwordResetUrl + "?token=" + token);
    emailSender.send(message);
  }
}