package com.hyunn.commerceplatform.security.handler;

import com.hyunn.commerceplatform.entity.Users;
import com.hyunn.commerceplatform.service.LoginLogService;
import com.hyunn.commerceplatform.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final UserService userService;
  private final LoginLogService loginLogService;

  @Override
  @Transactional
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    String username = authentication.getName();
    Users user = userService.getUserOrThrow(username);
    loginLogService.recordLoginLog(user);

    super.onAuthenticationSuccess(request, response, authentication);
  }

}