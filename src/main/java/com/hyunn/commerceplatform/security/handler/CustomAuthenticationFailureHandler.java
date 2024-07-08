package com.hyunn.commerceplatform.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyunn.commerceplatform.entity.Users;
import com.hyunn.commerceplatform.service.UserService;
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

  private final UserService userService;

  @Value("${app.login.max-attempts}")
  private int maxAttempts;

  @Value("${app.login.lock-duration-minutes}")
  private int lockDurationMinutes;

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception) throws IOException {
    String username = request.getParameter("username");
    String errorMessage;

    if (exception instanceof LockedException) {
      errorMessage = String.format("Account is locked. Please try again after %d minutes.",
          lockDurationMinutes);
    } else {
      Users user = userService.getUserOrThrow(username);
      if (user.isAccountNonLocked()) {
        if (user.getFailedAttempt() < maxAttempts - 1) {
          userService.incrementFailedAttempts(user);
          int attemptsLeft = maxAttempts - user.getFailedAttempt();
          errorMessage = String.format(
              "Invalid credentials. %d attempt%s left before account lockout.",
              attemptsLeft, attemptsLeft == 1 ? "" : "s");
        } else {
          userService.lockUser(user);
          errorMessage = String.format(
              "Account locked due to %d failed attempts. It will be unlocked after %d minutes.",
              maxAttempts, lockDurationMinutes);
        }
      } else {
        if (userService.unlockWhenTimeExpired(user)) {
          errorMessage = "Account has been unlocked. Please try to login again.";
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