package com.hyunn.commerceplatform.service;

import com.hyunn.commerceplatform.dto.business.BusinessRegistrationDto;

public interface BusinessService {

  void registerBusiness(String username, BusinessRegistrationDto dto);

  void deleteBusiness(Long businessId, String username);
}
