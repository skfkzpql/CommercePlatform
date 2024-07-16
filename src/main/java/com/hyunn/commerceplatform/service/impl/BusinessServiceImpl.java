package com.hyunn.commerceplatform.service.impl;

import com.hyunn.commerceplatform.dto.business.BusinessRegistrationDto;
import com.hyunn.commerceplatform.entity.Businesses;
import com.hyunn.commerceplatform.entity.Users;
import com.hyunn.commerceplatform.entity.types.UserType;
import com.hyunn.commerceplatform.exception.BusinessException;
import com.hyunn.commerceplatform.exception.UserException;
import com.hyunn.commerceplatform.repository.BusinessRepository;
import com.hyunn.commerceplatform.repository.UsersRepository;
import com.hyunn.commerceplatform.service.BusinessService;
import com.hyunn.commerceplatform.service.PublicDataVerificationService;
import com.hyunn.commerceplatform.util.ModelMapperUtil;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessService {

  private final PublicDataVerificationService verificationService;
  private final BusinessRepository businessRepository;
  private final UsersRepository usersRepository;

  @Transactional
  @Override
  public void registerBusiness(String username, BusinessRegistrationDto dto) {
    Users user = usersRepository.findByUsername(username)
        .orElseThrow(UserException::userNotFound);

    if (!Objects.equals(username, dto.getP_nm())) {
      throw BusinessException.usernameMismatch();
    }

    boolean isValid = verificationService.verifyBusinessRegistration(dto);
    if (!isValid) {
      throw BusinessException.invalidBusinessInfo();
    }

    if (businessRepository.existsByRegistrationNumber(dto.getB_no())) {
      throw BusinessException.businessAlreadyExists();
    }

    Businesses business = ModelMapperUtil.map(dto, Businesses.class);
    business.setUser(user);
    businessRepository.save(business);

    user.setUserType(UserType.BUSINESS);
    usersRepository.save(user);
  }

  @Override
  @Transactional
  public void deleteBusiness(Long businessId, String username) {
    Users user = usersRepository.findByUsername(username)
        .orElseThrow(UserException::userNotFound);

    Businesses business = businessRepository.findById(businessId)
        .orElseThrow(BusinessException::businessNotFound);

    if (!business.getUser().getUsername().equals(username)) {
      throw BusinessException.unauthorized();
    }

    businessRepository.delete(business);

    if (!businessRepository.existsByUser(user)) {
      user.setUserType(UserType.CONSUMER);
      usersRepository.save(user);
    }
  }
}