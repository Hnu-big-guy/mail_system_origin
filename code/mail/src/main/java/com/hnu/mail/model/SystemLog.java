// SystemLog.java
package com.hnu.mail.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "system_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  private LogType type;

  @Size(max = 100)
  private String module;

  @Size(max = 500)
  private String operation;

  @Size(max = 1000)
  private String details;

  @Size(max = 50)
  private String ipAddress;

  @Size(max = 100)
  private String username;

  private LocalDateTime createdAt = LocalDateTime.now();

  public enum LogType {
    LOGIN, LOGOUT, SEND_MAIL, RECEIVE_MAIL, SAVE_DRAFT,
    CREATE_USER, DELETE_USER, UPDATE_USER,
    SYSTEM_CONFIG, SECURITY, ERROR
  }
}