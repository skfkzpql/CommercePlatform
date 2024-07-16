package com.hyunn.commerceplatform.service;

import com.hyunn.commerceplatform.dto.business.BusinessRegistrationDto;

public interface PublicDataVerificationService {

  boolean verifyBusinessRegistration(BusinessRegistrationDto dto);
}