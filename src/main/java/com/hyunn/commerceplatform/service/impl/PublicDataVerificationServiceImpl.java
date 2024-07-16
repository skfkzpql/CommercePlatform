package com.hyunn.commerceplatform.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyunn.commerceplatform.dto.business.BusinessRegistrationDto;
import com.hyunn.commerceplatform.exception.PublicDataVerificationException;
import com.hyunn.commerceplatform.service.PublicDataVerificationService;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class PublicDataVerificationServiceImpl implements PublicDataVerificationService {

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  @Value("${public.data.api.url}")
  private String apiUrl;

  @Value("${public.data.api.key}")
  private String serviceKey;

  private static @NotNull Map<String, Object> getStringObjectMap(BusinessRegistrationDto dto) {
    Map<String, Object> businessData = new HashMap<>();
    businessData.put("b_no", dto.getB_no());
    businessData.put("start_dt", dto.getStart_dt());
    businessData.put("p_nm", dto.getP_nm());

    // 선택적 필드들은 null이 아닌 경우에만 추가
    if (dto.getP_nm2() != null) {
      businessData.put("p_nm2", dto.getP_nm2());
    }
    if (dto.getB_nm() != null) {
      businessData.put("b_nm", dto.getB_nm());
    }
    if (dto.getCorp_no() != null) {
      businessData.put("corp_no", dto.getCorp_no());
    }
    if (dto.getB_sector() != null) {
      businessData.put("b_sector", dto.getB_sector());
    }
    if (dto.getB_type() != null) {
      businessData.put("b_type", dto.getB_type());
    }
    if (dto.getB_adr() != null) {
      businessData.put("b_adr", dto.getB_adr());
    }
    return businessData;
  }

  @Override
  public boolean verifyBusinessRegistration(BusinessRegistrationDto dto) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    Map<String, Object> businessData = getStringObjectMap(dto);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("businesses", Collections.singletonList(businessData));

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

    String url = apiUrl + "/validate?serviceKey=" + serviceKey;

    ResponseEntity<String> response = restTemplate.exchange(
        url,
        HttpMethod.POST,
        entity,
        String.class
    );

    if (response.getStatusCode().is2xxSuccessful()) {
      return parseResponse(response.getBody());
    } else {
      throw PublicDataVerificationException.apiCallFailed(response.getStatusCode().toString());
    }
  }

  private boolean parseResponse(String responseBody) {
    try {
      JsonNode root = objectMapper.readTree(responseBody);
      String statusCode = root.path("status_code").asText();
      if (!"OK".equals(statusCode)) {
        throw PublicDataVerificationException.invalidResponseStatus(statusCode);
      }

      JsonNode dataNode = root.path("data").get(0);
      String valid = dataNode.path("valid").asText();
      return "01".equals(valid);
    } catch (Exception e) {
      throw PublicDataVerificationException.responseParsingFailed(e.getMessage());
    }
  }
}