// LogService.java
package com.hnu.mail.service;

import com.hnu.mail.model.SystemLog;
import com.hnu.mail.repository.SystemLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LogService {

  private final SystemLogRepository logRepository;

  /**
   * 创建日志记录
   */
  public void createLog(SystemLog.LogType type, String module, String operation, 
                       String details, String ipAddress, String username) {
    SystemLog log = new SystemLog();
    log.setType(type);
    log.setModule(module);
    log.setOperation(operation);
    log.setDetails(details);
    log.setIpAddress(ipAddress);
    log.setUsername(username);
    log.setCreatedAt(LocalDateTime.now());
    
    logRepository.save(log);
  }

  /**
   * 分页获取所有日志
   */
  public Page<SystemLog> getAllLogs(Pageable pageable) {
    return logRepository.findAll(pageable);
  }

  /**
   * 按类型获取日志
   */
  public Page<SystemLog> getLogsByType(SystemLog.LogType type, Pageable pageable) {
    return logRepository.findByType(type, pageable);
  }

  /**
   * 按模块获取日志
   */
  public Page<SystemLog> getLogsByModule(String module, Pageable pageable) {
    return logRepository.findByModule(module, pageable);
  }

  /**
   * 按用户名获取日志
   */
  public Page<SystemLog> getLogsByUsername(String username, Pageable pageable) {
    return logRepository.findByUsername(username, pageable);
  }

  /**
   * 删除指定日期之前的所有日志
   */
  @Transactional
  public void deleteOldLogs(LocalDateTime beforeDate) {
    logRepository.deleteAllByCreatedAtBefore(beforeDate);
  }

  /**
   * 清空所有日志
   */
  @Transactional
  public void clearAllLogs() {
    logRepository.deleteAll();
  }
}
