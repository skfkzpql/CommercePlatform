package com.hyunn.commerceplatform.service.impl;

import com.hyunn.commerceplatform.entity.LoginLog;
import com.hyunn.commerceplatform.entity.Users;
import com.hyunn.commerceplatform.repository.LoginLogRepository;
import com.hyunn.commerceplatform.service.LoginLogService;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogServiceImpl implements LoginLogService {

  private final HttpServletRequest request;
  private final LoginLogRepository loginLogRepository;

  @Value("${geoip.database.path}")
  private String geoIpDatabasePath;

  @Value("${login.log.retention.days}")
  private int loginLogRetentionDays;

  @Override
  public void recordLoginLog(Users user) {
    String ip = getClientIpAddress();
    String location = getLocationFromIp(ip);

    LoginLog loginLog = new LoginLog();
    loginLog.setUser(user);
    loginLog.setIpAddress(ip);
    loginLog.setLocation(location);
    loginLog.setLoginTime(LocalDateTime.now());
    loginLogRepository.save(loginLog);
  }

  private String getClientIpAddress() {
    String ipAddress = request.getHeader("X-Forwarded-For");
    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getHeader("Proxy-Client-IP");
    }
    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getRemoteAddr();
    }
    return ipAddress;
  }

  private String getLocationFromIp(String ipAddress) {
    File database = new File(geoIpDatabasePath);
    try (DatabaseReader reader = new DatabaseReader.Builder(database).build()) {
      InetAddress inetAddress = InetAddress.getByName(ipAddress);
      CityResponse response = reader.city(inetAddress);

      String countryName = response.getCountry().getName();
      String cityName = response.getCity().getName();

      return cityName != null && !cityName.isEmpty()
          ? cityName + ", " + countryName
          : countryName;
    } catch (IOException | GeoIp2Exception e) {
      log.error("Error getting location from IP: {}", ipAddress, e);
      return "Unknown";
    }
  }

  // 스케줄링된 작업
  @Scheduled(cron = "${login.log.cleanup.cron}")
  @Transactional
  public void cleanupOldLoginLogs() {
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(loginLogRetentionDays);
    int deletedCount = loginLogRepository.deleteOlderThan(cutoffDate);
    log.info("Cleaned up {} login logs older than {}", deletedCount, cutoffDate);
  }
}