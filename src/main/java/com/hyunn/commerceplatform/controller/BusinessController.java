package com.hyunn.commerceplatform.controller;

import com.hyunn.commerceplatform.dto.business.BusinessRegistrationDto;
import com.hyunn.commerceplatform.service.BusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/business")
@RequiredArgsConstructor
public class BusinessController {

  private final BusinessService businessService;

  @PostMapping("register")
  public ResponseEntity<?> registerBusiness(@RequestBody BusinessRegistrationDto dto) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    businessService.registerBusiness(authentication.getName(), dto);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @DeleteMapping("/{businessId}")
  public ResponseEntity<Void> deleteBusiness(@PathVariable Long businessId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    businessService.deleteBusiness(businessId, authentication.getName());
    return ResponseEntity.ok().build();
  }
}
