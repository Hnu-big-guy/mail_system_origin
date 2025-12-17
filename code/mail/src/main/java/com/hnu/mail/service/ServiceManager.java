// ServiceManager.java
package com.hnu.mail.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hnu.mail.model.SystemConfig;
import com.hnu.mail.model.SystemLog;

@Service
public class ServiceManager {

  private final SystemConfigService configService;
  private final LogService logService;
  private final Pop3Service pop3Service; // POP3客户端服务
  private final Pop3Server pop3Server;   // POP3服务器
  private final SmtpServer smtpServer;   // SMTP服务器
  
  @Autowired
  public ServiceManager(SystemConfigService configService, LogService logService,
                       Pop3Service pop3Service, Pop3Server pop3Server, SmtpServer smtpServer) {
      this.configService = configService;
      this.logService = logService;
      this.pop3Service = pop3Service;
      this.pop3Server = pop3Server;
      this.smtpServer = smtpServer;
  }

  // SMTP服务状态
  private boolean smtpServiceEnabled = true;
  // POP3服务状态
  private boolean pop3ServiceEnabled = true;
  // POP3服务器状态
  private boolean pop3ServerEnabled = true;
  // SMTP服务器状态
  private boolean smtpServerEnabled = true;

  /**
   * 初始化服务状态
   */
  public void initializeServices() {
    // 从配置中加载服务状态
    smtpServiceEnabled = Boolean.parseBoolean(
        configService.getConfigValue(SystemConfig.SMTP_SERVICE_ENABLED, "true"));
    pop3ServiceEnabled = Boolean.parseBoolean(
        configService.getConfigValue(SystemConfig.POP3_SERVICE_ENABLED, "true"));
    pop3ServerEnabled = Boolean.parseBoolean(
        configService.getConfigValue(SystemConfig.POP3_SERVER_ENABLED, "true"));
    smtpServerEnabled = Boolean.parseBoolean(
        configService.getConfigValue(SystemConfig.SMTP_SERVER_ENABLED, "true"));

    // 根据配置启动或停止服务
    if (!pop3ServiceEnabled) {
      pop3Service.stop();
    }
    
    // 根据配置启动或停止服务器
    if (pop3ServerEnabled) {
      pop3Server.start();
    }
    
    if (smtpServerEnabled) {
      smtpServer.start();
    }
  }

  /**
   * 启动SMTP服务
   */
  public synchronized void startSmtpService() {
    smtpServiceEnabled = true;
    configService.updateConfig(
        "service.smtp.enabled", "true", "是否启用SMTP服务", "system");

    logService.createLog(
        SystemLog.LogType.SYSTEM_CONFIG,
        "服务管理",
        "启动SMTP服务",
        "SMTP服务已启动",
        "127.0.0.1",
        "system"
    );
  }

  /**
   * 停止SMTP服务
   */
  public synchronized void stopSmtpService() {
    smtpServiceEnabled = false;
    configService.updateConfig(
        "service.smtp.enabled", "false", "是否启用SMTP服务", "system");

    logService.createLog(
        SystemLog.LogType.SYSTEM_CONFIG,
        "服务管理",
        "停止SMTP服务",
        "SMTP服务已停止",
        "127.0.0.1",
        "system"
    );
  }

  /**
   * 启动SMTP服务器
   */
  public synchronized void startSmtpServer() {
    smtpServerEnabled = true;
    smtpServer.start();
    configService.updateConfig(
        "server.smtp.enabled", "true", "是否启用SMTP服务器", "system");

    logService.createLog(
        SystemLog.LogType.SYSTEM_CONFIG,
        "服务管理",
        "启动SMTP服务器",
        "SMTP服务器已启动",
        "127.0.0.1",
        "system"
    );
  }

  /**
   * 停止SMTP服务器
   */
  public synchronized void stopSmtpServer() {
    smtpServerEnabled = false;
    smtpServer.stop();
    configService.updateConfig(
        "server.smtp.enabled", "false", "是否启用SMTP服务器", "system");

    logService.createLog(
        SystemLog.LogType.SYSTEM_CONFIG,
        "服务管理",
        "停止SMTP服务器",
        "SMTP服务器已停止",
        "127.0.0.1",
        "system"
    );
  }

  /**
   * 启动POP3服务
   */
  public synchronized void startPop3Service() {
    pop3ServiceEnabled = true;
    configService.updateConfig(
        "service.pop3.enabled", "true", "是否启用POP3服务", "system");
    
    pop3Service.start();

    logService.createLog(
        SystemLog.LogType.SYSTEM_CONFIG,
        "服务管理",
        "启动POP3服务",
        "POP3服务已启动",
        "127.0.0.1",
        "system"
    );
  }

  /**
   * 停止POP3服务
   */
  public synchronized void stopPop3Service() {
    pop3ServiceEnabled = false;
    configService.updateConfig(
        "service.pop3.enabled", "false", "是否启用POP3服务", "system");
    
    pop3Service.stop();

    logService.createLog(
        SystemLog.LogType.SYSTEM_CONFIG,
        "服务管理",
        "停止POP3服务",
        "POP3服务已停止",
        "127.0.0.1",
        "system"
    );
  }

  /**
   * 获取SMTP服务状态
   */
  public boolean isSmtpServiceEnabled() {
    return smtpServiceEnabled;
  }

  /**
   * 获取POP3服务状态
   */
  public boolean isPop3ServiceEnabled() {
    return pop3ServiceEnabled;
  }

  /**
   * 获取POP3服务器状态
   */
  public boolean isPop3ServerEnabled() {
    return pop3ServerEnabled;
  }

  /**
   * 获取SMTP服务器状态
   */
  public boolean isSmtpServerEnabled() {
    return smtpServerEnabled;
  }

  /**
   * 启动POP3服务器
   */
  public synchronized void startPop3Server() {
    pop3ServerEnabled = true;
    pop3Server.start();
    configService.updateConfig(
        "server.pop3.enabled", "true", "是否启用POP3服务器", "system");

    logService.createLog(
        SystemLog.LogType.SYSTEM_CONFIG,
        "服务管理",
        "启动POP3服务器",
        "POP3服务器已启动",
        "127.0.0.1",
        "system"
    );
  }

  /**
   * 停止POP3服务器
   */
  public synchronized void stopPop3Server() {
    pop3ServerEnabled = false;
    pop3Server.stop();
    configService.updateConfig(
        "server.pop3.enabled", "false", "是否启用POP3服务器", "system");

    logService.createLog(
        SystemLog.LogType.SYSTEM_CONFIG,
        "服务管理",
        "停止POP3服务器",
        "POP3服务器已停止",
        "127.0.0.1",
        "system"
    );
  }

  /**
   * 重启SMTP服务
   */
  public synchronized void restartSmtpService() {
    stopSmtpService();
    // 短暂延迟确保服务完全停止
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    startSmtpService();
  }

  /**
   * 重启POP3服务
   */
  public synchronized void restartPop3Service() {
    stopPop3Service();
    // 短暂延迟确保服务完全停止
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    startPop3Service();
  }

  /**
   * 重启POP3服务器
   */
  public synchronized void restartPop3Server() {
    stopPop3Server();
    // 短暂延迟确保服务完全停止
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    startPop3Server();
  }

  /**
   * 重启SMTP服务器
   */
  public synchronized void restartSmtpServer() {
    stopSmtpServer();
    // 短暂延迟确保服务完全停止
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    startSmtpServer();
  }
}
