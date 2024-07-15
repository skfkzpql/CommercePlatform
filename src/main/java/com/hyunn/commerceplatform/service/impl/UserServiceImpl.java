package com.hyunn.commerceplatform.service.impl;

import com.hyunn.commerceplatform.dto.auth.PasswordResetEmailRequestDto;
import com.hyunn.commerceplatform.dto.auth.RegistrationRequestDto;
import com.hyunn.commerceplatform.dto.auth.ResetPasswordRequestDto;
import com.hyunn.commerceplatform.dto.users.UserTermAgreementDto;
import com.hyunn.commerceplatform.dto.users.UsersDetailResponseDto;
import com.hyunn.commerceplatform.dto.users.UsersEmailUpdateRequestDto;
import com.hyunn.commerceplatform.dto.users.UsersPasswordChangeRequestDto;
import com.hyunn.commerceplatform.entity.Terms;
import com.hyunn.commerceplatform.entity.UserTerms;
import com.hyunn.commerceplatform.entity.Users;
import com.hyunn.commerceplatform.entity.types.UserType;
import com.hyunn.commerceplatform.exception.TokenException;
import com.hyunn.commerceplatform.exception.UserException;
import com.hyunn.commerceplatform.repository.TermsRepository;
import com.hyunn.commerceplatform.repository.UserTermsRepository;
import com.hyunn.commerceplatform.repository.UsersRepository;
import com.hyunn.commerceplatform.security.JwtTokenProvider.TokenType;
import com.hyunn.commerceplatform.service.EmailService;
import com.hyunn.commerceplatform.service.MandatoryTermsCacheService;
import com.hyunn.commerceplatform.service.TokenService;
import com.hyunn.commerceplatform.service.UserService;
import com.hyunn.commerceplatform.util.ModelMapperUtil;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

  private final UsersRepository userRepository;
  private final TermsRepository termsRepository;
  private final UserTermsRepository userTermsRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenService tokenService;
  private final EmailService emailService;
  private final MandatoryTermsCacheService mandatoryTermsCacheService;


  // 사용자 등록 관련 메서드
  @Override
  @Transactional
  public void registerUser(RegistrationRequestDto requestDto) {
    validateUniqueUsername(requestDto.getUsername());
    validateUniqueEmail(requestDto.getEmail());
    validateMandatoryTermsAgreement(requestDto.getTermAgreements());

    Users user = createUserFromDto(requestDto);
    user = userRepository.save(user);

    saveUserTermAgreements(user, requestDto.getTermAgreements());
    sendVerificationEmail(user);
  }

  private void validateUniqueUsername(String username) {
    if (userRepository.findByUsername(username).isPresent()) {
      throw UserException.usernameAlreadyExists();
    }
  }

  private void validateUniqueEmail(String email) {
    if (userRepository.findByEmail(email).isPresent()) {
      throw UserException.emailAlreadyExists();
    }
  }

  private void validateMandatoryTermsAgreement(List<UserTermAgreementDto> termAgreements) {
    Set<Long> mandatoryTermIds = mandatoryTermsCacheService.getMandatoryTermIds();
    Set<Long> agreedMandatoryTermIds = termAgreements.stream()
        .filter(UserTermAgreementDto::getAgreed)
        .map(UserTermAgreementDto::getTermId)
        .filter(mandatoryTermIds::contains)
        .collect(Collectors.toSet());

    if (mandatoryTermIds.size() != agreedMandatoryTermIds.size()) {
      throw UserException.mandatoryTermsNotAgreed();
    }
  }

  private Users createUserFromDto(RegistrationRequestDto dto) {
    Users user = ModelMapperUtil.map(dto, Users.class);
    user.setUserType(UserType.CONSUMER);
    user.setPassword(passwordEncoder.encode(dto.getPassword()));
    return user;
  }

  private void saveUserTermAgreements(Users user, List<UserTermAgreementDto> termAgreements) {
    Map<Long, Terms> termsMap = termsRepository.findAllById(
        termAgreements.stream().map(UserTermAgreementDto::getTermId).collect(Collectors.toList())
    ).stream().collect(Collectors.toMap(Terms::getId, Function.identity()));

    List<UserTerms> userTermsList = termAgreements.stream()
        .map(agreementDto -> createUserTerm(user, agreementDto, termsMap))
        .collect(Collectors.toList());

    userTermsRepository.saveAll(userTermsList);
  }

  private UserTerms createUserTerm(Users user, UserTermAgreementDto agreementDto,
      Map<Long, Terms> termsMap) {
    Terms term = termsMap.get(agreementDto.getTermId());
    if (term == null) {
      throw UserException.invalidTermId(agreementDto.getTermId());
    }
    return UserTerms.builder()
        .user(user)
        .term(term)
        .agreed(agreementDto.getAgreed())
        .build();
  }

  // 사용자 정보 조회 및 수정 관련 메서드
  @Override
  public UsersDetailResponseDto getUserByUsername(String username) {
    Users user = getUserOrThrow(username);
    return ModelMapperUtil.map(user, UsersDetailResponseDto.class);
  }

  @Override
  @Transactional
  public void deleteUser(String username) {
    Users user = getUserOrThrow(username);
    userRepository.delete(user);
  }

  @Override
  @Transactional
  public void updateEmail(String username, UsersEmailUpdateRequestDto dto) {
    Users user = getUserOrThrow(username);
    validateUniqueEmail(dto.getNewEmail());
    updateUserEmail(user, dto.getNewEmail());
  }

  @Override
  @Transactional
  public void updatePassword(String username, UsersPasswordChangeRequestDto dto) {
    Users user = getUserOrThrow(username);
    validatePasswordChange(user, dto);
    updateUserPassword(user, dto.getNewPassword());
  }

  private void validatePasswordChange(Users user, UsersPasswordChangeRequestDto dto) {
    if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
      throw UserException.invalidCurrentPassword();
    }
    if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
      throw UserException.passwordConfirmMismatch();
    }
  }

  private void updateUserPassword(Users user, String newPassword) {
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
  }

  private void updateUserEmail(Users user, String newEmail) {
    user.setEmail(newEmail);
    user.setEmailVerified(false);
    userRepository.save(user);
    sendVerificationEmail(user);
  }

  @Async
  @Override
  public CompletableFuture<?> sendPasswordResetLink(PasswordResetEmailRequestDto dto) {
    return CompletableFuture.runAsync(() -> {
      Users user = getUserOrThrow(dto.getUsername());
      validateEmailMatch(user, dto.getEmail());
      String resetToken = generateAndSaveResetToken(user);
      try {
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
      } catch (Exception e) {
        log.error("Failed to send password reset email to user: {}", user.getUsername(), e);
        throw new RuntimeException("Failed to send password reset email", e);
      }
    });
  }

  @Override
  @Transactional
  public void resetPassword(ResetPasswordRequestDto dto) {
    String username = validateAndExtractUsernameFromToken(dto.getToken(), TokenType.PASSWORD_RESET);
    Users user = getUserOrThrow(username);
    validateNewPasswordMatch(dto);
    updateUserPassword(user, dto.getNewPassword());
    tokenService.removeTokenFromRedis(TokenType.PASSWORD_RESET, username);
  }

  private void validateEmailMatch(Users user, String email) {
    if (!user.getEmail().equals(email)) {
      throw UserException.emailMismatch();
    }
  }

  private String generateAndSaveResetToken(Users user) {
    String resetToken = tokenService.generateEmailToken(user.getUsername(), user.getEmail(),
        TokenType.PASSWORD_RESET);
    tokenService.saveTokenToRedis(TokenType.PASSWORD_RESET, user.getUsername(), resetToken);
    return resetToken;
  }

  // 이메일 인증 관련 메서드
  @Override
  @Transactional
  public void verifyEmail(String token) {
    String username = validateAndExtractUsernameFromToken(token, TokenType.EMAIL_VERIFICATION);
    Users user = getUserOrThrow(username);
    user.setEmailVerified(true);
    userRepository.save(user);
    tokenService.removeTokenFromRedis(TokenType.EMAIL_VERIFICATION, username);
  }

  @Async
  protected CompletableFuture<Void> sendVerificationEmail(Users user) {
    return CompletableFuture.runAsync(() -> {
      try {
        String emailToken = tokenService.generateEmailToken(user.getUsername(), user.getEmail(),
            TokenType.EMAIL_VERIFICATION);
        tokenService.saveTokenToRedis(TokenType.EMAIL_VERIFICATION, user.getUsername(), emailToken);
        emailService.sendVerificationEmail(user.getEmail(), emailToken);
      } catch (Exception e) {
        log.error("Failed to send verification email to user: {}", user.getUsername(), e);
        throw new RuntimeException("Failed to send verification email", e);
      }
    });
  }

  // 토큰 검증 관련 메서드
  private String validateAndExtractUsernameFromToken(String token, TokenType tokenType) {
    String username = tokenService.getUsernameFromToken(token);
    if (username == null || tokenService.isNotValidTokenFromRedis(tokenType, username, token)) {
      throw TokenException.invalidToken();
    }
    return username;
  }

  // 유틸리티 메서드
  public Users getUserOrThrow(String username) {
    return userRepository.findByUsername(username)
        .orElseThrow(UserException::userNotFound);
  }

  private void validateNewPasswordMatch(ResetPasswordRequestDto dto) {
    if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
      throw UserException.passwordConfirmMismatch();
    }
  }
}