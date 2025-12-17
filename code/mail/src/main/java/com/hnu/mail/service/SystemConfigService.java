// SystemConfigService.java
package com.hnu.mail.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hnu.mail.model.SystemConfig;
import com.hnu.mail.repository.SystemConfigRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SystemConfigService {

  private final SystemConfigRepository configRepository;
  private final LogService logService;

  /**
   * 获取所有系统配置
   */
  public List<SystemConfig> getAllConfigs() {
    return configRepository.findAll();
  }

  /**
   * 根据配置键获取配置
   */
  public Optional<SystemConfig> getConfigByKey(String configKey) {
    return configRepository.findByConfigKey(configKey);
  }

  /**
   * 获取配置值，如果不存在则返回默认值
   */
  public String getConfigValue(String configKey, String defaultValue) {
    return configRepository.findByConfigKey(configKey)
        .map(SystemConfig::getConfigValue)
        .orElse(defaultValue);
  }

  /**
   * 更新或创建配置
   */
  @Transactional
  public SystemConfig updateConfig(String configKey, String configValue, String description, String updatedBy) {
    SystemConfig config = configRepository.findByConfigKey(configKey)
        .orElse(new SystemConfig());

    config.setConfigKey(configKey);
    config.setConfigValue(configValue);
    config.setDescription(description);
    config.setUpdatedBy(updatedBy);
    config.setUpdatedAt(LocalDateTime.now());

    SystemConfig savedConfig = configRepository.save(config);

    // 记录配置变更日志
    logService.createLog(
        com.hnu.mail.model.SystemLog.LogType.SYSTEM_CONFIG,
        "系统配置",
        "更新配置参数",
        String.format("配置项: %s, 新值: %s", configKey, configValue),
        "127.0.0.1", // 实际应从请求中获取IP
        updatedBy
    );

    return savedConfig;
  }

  /**
   * 删除配置
   */
  @Transactional
  public void deleteConfig(String configKey, String deletedBy) {
    // 记录删除日志
    configRepository.findByConfigKey(configKey).ifPresent(config -> {
      logService.createLog(
          com.hnu.mail.model.SystemLog.LogType.SYSTEM_CONFIG,
          "系统配置",
          "删除配置参数",
          String.format("配置项: %s, 值: %s", configKey, config.getConfigValue()),
          "127.0.0.1", // 实际应从请求中获取IP
          deletedBy
      );
    });

    configRepository.deleteByConfigKey(configKey);
  }

  /**
   * 初始化系统默认配置
   */
  @Transactional
  public void initializeDefaultConfigs(String adminUsername) {
    // 检查是否已经有配置
    if (configRepository.count() > 0) {
      return;
    }

    // 添加默认配置
    addDefaultConfig(SystemConfig.SMTP_HOST, "smtp.qq.com", "SMTP服务器地址", adminUsername);
    addDefaultConfig(SystemConfig.SMTP_PORT, "587", "SMTP服务器端口", adminUsername);
    addDefaultConfig(SystemConfig.SMTP_SSL_ENABLED, "false", "是否启用SMTP SSL", adminUsername);
    addDefaultConfig(SystemConfig.SMTP_STARTTLS_ENABLED, "true", "是否启用SMTP STARTTLS", adminUsername);

    addDefaultConfig(SystemConfig.POP3_HOST, "pop.qq.com", "POP3服务器地址", adminUsername);
    addDefaultConfig(SystemConfig.POP3_PORT, "995", "POP3服务器端口", adminUsername);
    addDefaultConfig(SystemConfig.POP3_SSL_ENABLED, "true", "是否启用POP3 SSL", adminUsername);
    addDefaultConfig(SystemConfig.POP3_FETCH_INTERVAL, "300000", "POP3邮件拉取间隔(毫秒)", adminUsername);

    addDefaultConfig(SystemConfig.MAIL_DOMAIN, "test.com", "邮件域名", adminUsername);
    addDefaultConfig(SystemConfig.MAX_ATTACHMENT_SIZE, "10485760", "最大附件大小(字节)", adminUsername);
    addDefaultConfig(SystemConfig.DEFAULT_MAILBOX_SIZE, "100", "默认邮箱大小(MB)", adminUsername);
    addDefaultConfig(SystemConfig.MAX_MAIL_SIZE, "20971520", "最大邮件大小(字节)", adminUsername);

    addDefaultConfig(SystemConfig.LOG_LEVEL, "DEBUG", "日志级别", adminUsername);
    addDefaultConfig(SystemConfig.LOG_RETENTION_DAYS, "30", "日志保留天数", adminUsername);

    addDefaultConfig(SystemConfig.SMTP_SERVICE_ENABLED, "true", "是否启用SMTP服务", adminUsername);
    addDefaultConfig(SystemConfig.POP3_SERVICE_ENABLED, "true", "是否启用POP3服务", adminUsername);

    // POP3服务器默认配置
    addDefaultConfig(SystemConfig.POP3_SERVER_ENABLED, "true", "是否启用POP3服务器", adminUsername);
    addDefaultConfig(SystemConfig.POP3_SERVER_PORT, "110", "POP3服务器监听端口", adminUsername);
    addDefaultConfig(SystemConfig.POP3_SERVER_MAX_CONNECTIONS, "100", "POP3服务器最大连接数", adminUsername);

    // SMTP服务器默认配置
    addDefaultConfig(SystemConfig.SMTP_SERVER_ENABLED, "true", "是否启用SMTP服务器", adminUsername);
    addDefaultConfig(SystemConfig.SMTP_SERVER_PORT, "25", "SMTP服务器监听端口", adminUsername);
    addDefaultConfig(SystemConfig.SMTP_SERVER_DOMAIN, "localhost", "SMTP服务器域名", adminUsername);
    addDefaultConfig(SystemConfig.SMTP_SERVER_MAX_CONNECTIONS, "100", "SMTP服务器最大连接数", adminUsername);
  }

  private void addDefaultConfig(String key, String value, String description, String username) {
    SystemConfig config = new SystemConfig();
    config.setConfigKey(key);
    config.setConfigValue(value);
    config.setDescription(description);
    config.setUpdatedBy(username);
    config.setUpdatedAt(LocalDateTime.now());
    configRepository.save(config);
  }
}
