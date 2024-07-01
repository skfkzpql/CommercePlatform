package com.hyunn.commerceplatform.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

  @Value("${app.security.jwt.secret}")
  private String secretKey;

  @Value("${app.security.jwt.access-token-expiration-milliseconds}")
  private long accessTokenExpirationInMs;

  @Value("${app.security.jwt.refresh-token-expiration-milliseconds}")
  private long refreshTokenExpirationInMs;

  @Value("${app.security.token.email-verification-validity-seconds}")
  private long emailTokenValiditySeconds;

  private SecretKey key;

  @PostConstruct
  protected void init() {
    this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
  }

  public String generateAuthToken(Authentication authentication, TokenType tokenType) {
    String username = authentication.getName();
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() +
        (tokenType == TokenType.ACCESS ? accessTokenExpirationInMs : refreshTokenExpirationInMs));

    String authorities = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.joining(","));

    return Jwts.builder()
        .setSubject(username)
        .claim("authorities", authorities)
        .claim("tokenType", tokenType.name())
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(key)
        .compact();
  }

  public String generateEmailToken(String username, String email, TokenType tokenType) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + emailTokenValiditySeconds * 1000);

    return Jwts.builder()
        .setSubject(username)
        .claim("email", email)
        .claim("tokenType", tokenType.name())
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(key)
        .compact();
  }

  public String getUsernameFromToken(String token) {
    Claims claims = Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody();

    return claims.getSubject();
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  public Authentication getAuthentication(String token) {
    Claims claims = Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody();

    Collection<? extends GrantedAuthority> authorities =
        Arrays.stream(claims.get("authorities").toString().split(","))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

    User principal = new User(claims.getSubject(), "", authorities);

    return new UsernamePasswordAuthenticationToken(principal, token, authorities);
  }

  public Claims getClaimsFromToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  public String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

  public enum TokenType {
    ACCESS, REFRESH, EMAIL_VERIFICATION, PASSWORD_RESET
  }
}