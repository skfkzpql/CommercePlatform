package com.hyunn.commerceplatform.controller;

import com.hyunn.commerceplatform.dto.users.UsersDetailResponseDto;
import com.hyunn.commerceplatform.dto.users.UsersEmailUpdateRequestDto;
import com.hyunn.commerceplatform.dto.users.UsersPasswordChangeRequestDto;
import com.hyunn.commerceplatform.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersController {

  private UserService userService;

  @GetMapping("/detail")
  public ResponseEntity<?> getUserDetail() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UsersDetailResponseDto user = userService.getUserByUsername(authentication.getName());
    return ResponseEntity.status(HttpStatus.OK).body(user);
  }

  @DeleteMapping("/withdraw")
  public ResponseEntity<?> deleteUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    userService.deleteUser(authentication.getName());
    SecurityContextHolder.clearContext();
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @PutMapping("/update-email")
  public ResponseEntity<?> updateEmail(
      @Valid @RequestBody UsersEmailUpdateRequestDto usersEmailUpdateRequestDto) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    userService.updateEmail(authentication.getName(), usersEmailUpdateRequestDto);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PostMapping("/update-password")
  public ResponseEntity<?> updatePassword(
      @Valid @RequestBody UsersPasswordChangeRequestDto usersPasswordChangeRequestDto) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    userService.updatePassword(authentication.getName(), usersPasswordChangeRequestDto);
    return ResponseEntity.status(HttpStatus.OK).build();
  }


}
