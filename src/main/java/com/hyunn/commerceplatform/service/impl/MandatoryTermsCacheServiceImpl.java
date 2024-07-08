package com.hyunn.commerceplatform.service.impl;

import com.hyunn.commerceplatform.entity.Terms;
import com.hyunn.commerceplatform.entity.types.TermType;
import com.hyunn.commerceplatform.repository.TermsRepository;
import com.hyunn.commerceplatform.service.MandatoryTermsCacheService;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MandatoryTermsCacheServiceImpl implements MandatoryTermsCacheService {

  private final TermsRepository termsRepository;

  @Cacheable(value = "mandatoryTerms", key = "'ids'")
  public Set<Long> getMandatoryTermIds() {
    return termsRepository.findByType(TermType.MANDATORY).stream()
        .map(Terms::getId)
        .collect(Collectors.toSet());
  }
}