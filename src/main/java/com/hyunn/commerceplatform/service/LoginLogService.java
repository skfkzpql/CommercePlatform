package com.hyunn.commerceplatform.service;

import com.hyunn.commerceplatform.entity.Users;

public interface LoginLogService {

  void recordLoginLog(Users user);

  void cleanupOldLoginLogs();
}