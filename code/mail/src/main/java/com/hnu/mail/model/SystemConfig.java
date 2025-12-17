// SystemConfig.java
package com.hnu.mail.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "system_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfig {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Size(max = 100)
  @Column(unique = true, nullable = false)
  private String configKey;

  @Size(max = 500)
  private String configValue;

  @Size(max = 200)
  private String description;

  @Size(max = 50)
  private String updatedBy;

  private LocalDateTime updatedAt = LocalDateTime.now();

  // 常用配置键常量
  public static final String SMTP_HOST = "smtp.host";
  public static final String SMTP_PORT = "smtp.port";
  public static final String SMTP_USERNAME = "smtp.username";
  public static final String SMTP_PASSWORD = "smtp.password";
  public static final String SMTP_SSL_ENABLED = "smtp.ssl.enabled";
  public static final String SMTP_STARTTLS_ENABLED = "smtp.starttls.enabled";

  public static final String POP3_HOST = "pop3.host";
  public static final String POP3_PORT = "pop3.port";
  public static final String POP3_SSL_ENABLED = "pop3.ssl.enabled";
  public static final String POP3_FETCH_INTERVAL = "pop3.fetch.interval";

  public static final String MAIL_DOMAIN = "mail.domain";
  public static final String MAX_ATTACHMENT_SIZE = "mail.max.attachment.size";
  public static final String DEFAULT_MAILBOX_SIZE = "mail.default.mailbox.size";
  public static final String MAX_MAIL_SIZE = "mail.max.size";

  public static final String LOG_LEVEL = "log.level";
  public static final String LOG_RETENTION_DAYS = "log.retention.days";

  public static final String SMTP_SERVICE_ENABLED = "service.smtp.enabled";
  public static final String POP3_SERVICE_ENABLED = "service.pop3.enabled";

  // POP3服务器配置
  public static final String POP3_SERVER_ENABLED = "server.pop3.enabled";
  public static final String POP3_SERVER_PORT = "server.pop3.port";
  public static final String POP3_SERVER_MAX_CONNECTIONS = "server.pop3.max.connections";

  // SMTP服务器配置
  public static final String SMTP_SERVER_ENABLED = "server.smtp.enabled";
  public static final String SMTP_SERVER_PORT = "server.smtp.port";
  public static final String SMTP_SERVER_DOMAIN = "server.smtp.domain";
  public static final String SMTP_SERVER_MAX_CONNECTIONS = "server.smtp.max.connections";
}
