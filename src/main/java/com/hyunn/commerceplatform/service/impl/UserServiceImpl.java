package com.hyunn.commerceplatform.service.impl;

import com.hyunn.commerceplatform.dto.auth.LoginRequestDto;
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
import com.hyunn.commerceplatform.entity.types.TermType;
import com.hyunn.commerceplatform.exception.TokenException;
import com.hyunn.commerceplatform.exception.UserException;
import com.hyunn.commerceplatform.repository.TermsRepository;
import com.hyunn.commerceplatform.repository.UserTermsRepository;
import com.hyunn.commerceplatform.repository.UsersRepository;
import com.hyunn.commerceplatform.security.JwtTokenProvider.TokenType;
import com.hyunn.commerceplatform.service.EmailService;
import com.hyunn.commerceplatform.service.TokenService;
import com.hyunn.commerceplatform.service.UserService;
import com.hyunn.commerceplatform.util.ModelMapperUtil;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UsersRepository userRepository;
  private final TermsRepository termsRepository;
  private final UserTermsRepository userTermsRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenService tokenService;
  private final EmailService emailService;
  private final CacheManager cacheManager;


  @Override
  @Transactional
  public void registerUser(RegistrationRequestDto requestDto) {
    if (userRepository.findByUsername(requestDto.getUsername()).isPresent()) {
      throw UserException.usernameAlreadyExists();
    }
    if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
      throw UserException.emailAlreadyExists();
    }

    validateMandatoryTermsAgreement(requestDto.getTermAgreements());

    Users user = ModelMapperUtil.map(requestDto, Users.class);
    user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
    user = userRepository.save(user);

    saveUserTermAgreements(user, requestDto.getTermAgreements());

    String emailToken = tokenService.generateEmailToken(user.getUsername(), user.getEmail(),
        TokenType.EMAIL_VERIFICATION);
    tokenService.saveTokenToRedis(TokenType.EMAIL_VERIFICATION, user.getUsername(), emailToken);
    emailService.sendVerificationEmail(user.getEmail(), emailToken);
  }

  private void validateMandatoryTermsAgreement(List<UserTermAgreementDto> termAgreements) {
    Set<Long> mandatoryTermIds = getMandatoryTermIds();
    Set<Long> agreedMandatoryTermIds = termAgreements.stream()
        .filter(UserTermAgreementDto::getAgreed)
        .map(UserTermAgreementDto::getTermId)
        .filter(mandatoryTermIds::contains)
        .collect(Collectors.toSet());

    if (mandatoryTermIds.size() != agreedMandatoryTermIds.size()) {
      throw UserException.mandatoryTermsNotAgreed();
    }
  }

  private void saveUserTermAgreements(Users user, List<UserTermAgreementDto> termAgreements) {
    Map<Long, Terms> termsMap = termsRepository.findAllById(
        termAgreements.stream().map(UserTermAgreementDto::getTermId).collect(Collectors.toList())
    ).stream().collect(Collectors.toMap(Terms::getId, Function.identity()));

    List<UserTerms> userTermsList = termAgreements.stream()
        .map(agreementDto -> {
          Terms term = termsMap.get(agreementDto.getTermId());
          if (term == null) {
            throw UserException.invalidTermId(agreementDto.getTermId());
          }
          return UserTerms.builder()
              .user(user)
              .term(term)
              .agreed(agreementDto.getAgreed())
              .build();
        })
        .collect(Collectors.toList());

    userTermsRepository.saveAll(userTermsList);
  }

  private Set<Long> getMandatoryTermIds() {
    Cache cache = cacheManager.getCache("mandatoryTerms");
    if (cache != null) {
      Set<Long> cachedIds = cache.get("ids", Set.class);
      if (cachedIds != null && cachedIds.stream().allMatch(id -> id instanceof Long)) {
        return new HashSet<>(cachedIds);
      }
    }

    Set<Long> mandatoryTermIds = termsRepository.findByType(TermType.MANDATORY).stream()
        .map(Terms::getId)
        .collect(Collectors.toSet());

    if (cache != null) {
      cache.put("ids", mandatoryTermIds);
    }

    return mandatoryTermIds;
  }

  @Override
  public void loginUser(LoginRequestDto dto) {
    Users user = userRepository.findByUsername(dto.getUsername()).orElseThrow(
        UserException::userNotFound);

    if (!user.getEmailVerified()) {
      if (tokenService.isTokenPresentInRedis(TokenType.EMAIL_VERIFICATION, user.getUsername())) {
        tokenService.removeTokenFromRedis(TokenType.EMAIL_VERIFICATION, user.getUsername());
      }
      String newToken = tokenService.generateEmailToken(user.getUsername(), user.getEmail(),
          TokenType.EMAIL_VERIFICATION);
      tokenService.saveTokenToRedis(TokenType.EMAIL_VERIFICATION, user.getUsername(), newToken);
      emailService.sendVerificationEmail(user.getEmail(), newToken);
    }
  }

  @Override
  public UsersDetailResponseDto getUserByUsername(String username) {
    Users user = userRepository.findByUsername(username)
        .orElseThrow(UserException::userNotFound);
    return ModelMapperUtil.map(user, UsersDetailResponseDto.class);
  }

  @Override
  @Transactional
  public void deleteUser(String username) {
    Users user = userRepository.findByUsername(username)
        .orElseThrow(UserException::userNotFound);
    userRepository.delete(user);
  }

  @Override
  @Transactional
  public void updateEmail(String username, UsersEmailUpdateRequestDto dto) {
    Users user = userRepository.findByUsername(username)
        .orElseThrow(UserException::userNotFound);
    if (userRepository.findByEmail(dto.getNewEmail()).isPresent()) {
      throw UserException.emailAlreadyExists();
    }
    user.setEmail(dto.getNewEmail());
    user.setEmailVerified(false);
    user = userRepository.save(user);

    String emailToken = tokenService.generateEmailToken(user.getUsername(), user.getEmail(),
        TokenType.EMAIL_VERIFICATION);
    if (tokenService.isTokenPresentInRedis(TokenType.EMAIL_VERIFICATION, user.getUsername())) {
      tokenService.removeTokenFromRedis(TokenType.EMAIL_VERIFICATION, user.getUsername());
    }
    tokenService.saveTokenToRedis(TokenType.EMAIL_VERIFICATION, user.getUsername(), emailToken);
    emailService.sendVerificationEmail(user.getEmail(), emailToken);
  }

  @Override
  @Transactional
  public void updatePassword(String username, UsersPasswordChangeRequestDto dto) {
    Users user = userRepository.findByUsername(username)
        .orElseThrow(UserException::userNotFound);
    if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
      throw UserException.invalidCurrentPassword();
    }
    if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
      throw UserException.passwordConfirmMismatch();
    }
    user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    userRepository.save(user);
  }

  @Override
  public void sendPasswordResetLink(PasswordResetEmailRequestDto dto) {
    Users user = userRepository.findByUsername(dto.getUsername())
        .orElseThrow(UserException::userNotFound);
    if (!user.getEmail().equals(dto.getEmail())) {
      throw UserException.emailMismatch();
    }
    if (tokenService.isTokenPresentInRedis(TokenType.PASSWORD_RESET, user.getUsername())) {
      tokenService.removeTokenFromRedis(TokenType.PASSWORD_RESET, user.getUsername());
    }
    String resetToken = tokenService.generateEmailToken(user.getUsername(), user.getEmail(),
        TokenType.PASSWORD_RESET);
    tokenService.saveTokenToRedis(TokenType.PASSWORD_RESET, user.getUsername(), resetToken);
    emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
  }

  @Override
  @Transactional
  public void resetPassword(ResetPasswordRequestDto dto) {
    String username = tokenService.getUsernameFromToken(dto.getToken());
    if (username == null) {
      throw TokenException.invalidToken();
    }

    if (tokenService.isNotValidTokenFromRedis(TokenType.PASSWORD_RESET, username, dto.getToken())) {
      throw TokenException.invalidToken();
    }

    Users user = userRepository.findByUsername(username)
        .orElseThrow(UserException::userNotFound);

    if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
      throw UserException.passwordConfirmMismatch();
    }

    user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    userRepository.save(user);
    tokenService.removeTokenFromRedis(TokenType.PASSWORD_RESET, username);
  }

  @Override
  @Transactional
  public void verifyEmail(String token) {
    String username = tokenService.getUsernameFromToken(token);
    if (tokenService.isNotValidTokenFromRedis(TokenType.EMAIL_VERIFICATION, username, token)) {
      throw TokenException.invalidToken();
    }
    Users user = userRepository.findByUsername(username)
        .orElseThrow(UserException::userNotFound);
    user.setEmailVerified(true);
    userRepository.save(user);
    tokenService.removeTokenFromRedis(TokenType.EMAIL_VERIFICATION, username);
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository.findByUsername(username)
        .orElseThrow(
            () -> new UsernameNotFoundException("User not found with username: " + username));
  }
}