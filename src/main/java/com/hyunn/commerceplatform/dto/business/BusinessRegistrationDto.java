package com.hyunn.commerceplatform.dto.business;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class BusinessRegistrationDto {

  @NotBlank
  @Pattern(regexp = "^\\d{10}$")
  private String b_no; // 사업자등록번호 (필수)

  @NotBlank
  @Pattern(regexp = "^\\d{8}$")
  private String start_dt; // 개업일자 (필수)

  @NotBlank
  private String p_nm; // 대표자성명 (필수)

  private String p_nm2; // 대표자성명2 (선택)
  private String b_nm; // 상호 (선택)
  private String corp_no; // 법인등록번호 (선택)
  private String b_sector; // 주업태명 (선택)
  private String b_type; // 주종목명 (선택)
  private String b_adr; // 사업장주소 (선택)
}