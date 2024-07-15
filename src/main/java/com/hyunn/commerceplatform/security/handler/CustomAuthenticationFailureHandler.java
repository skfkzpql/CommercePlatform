package com.hyunn.commerceplatform.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyunn.commerceplatform.entity.Users;
import com.hyunn.commerceplatform.service.UserLockService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

  private final UserLockService userLockService;

  @Value("${app.login.max-fail-attempts}")
  private int maxAttempts;

  @Value("${app.login.lock-duration-minutes}")
  private int lockDurationMinutes;

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception) throws IOException {
    String username = request.getParameter("username");
    String errorMessage;
    Users user = userLockService.getUserOrThrow(username);
    if (exception instanceof LockedException) {

      errorMessage = String.format("Account is locked. Please try again after %d minutes.",
          lockDurationMinutes);
    } else {

      if (user.isAccountNonLocked()) {
        if (user.getFailedAttempt() < maxAttempts - 1) {
          userLockService.incrementFailedAttempts(user);
          int attemptsLeft = maxAttempts - user.getFailedAttempt();
          errorMessage = String.format(
              "Invalid credentials. %d attempt%s left before account lockout.",
              attemptsLeft, attemptsLeft == 1 ? "" : "s");
        } else {
          userLockService.lockUser(user);
          errorMessage = String.format(
              "Account locked due to %d failed attempts. It will be unlocked after %d minutes.",
              maxAttempts, lockDurationMinutes);
        }
      } else {

        if (userLockService.unlockWhenTimeExpired(user)) {
          errorMessage = "Account has been unlocked. Please tr y to login again.";
        } else {
          errorMessage = String.format("Account is still locked. Please try again after %d minutes",
              lockDurationMinutes);
        }
      }
    }

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    Map<String, String> data = new HashMap<>();
    data.put("error", "Authentication Failed");
    data.put("message", errorMessage);
    response.getOutputStream().println(new ObjectMapper().writeValueAsString(data));
  }
}