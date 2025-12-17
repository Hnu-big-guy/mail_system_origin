// SystemLogDto.java
package com.hnu.mail.dto;

import java.time.LocalDateTime;

import com.hnu.mail.model.SystemLog;

import lombok.Data;

@Data
public class SystemLogDto {
  private Long id;
  private SystemLog.LogType type;
  private String module;
  private String operation;
  private String details;
  private String ipAddress;
  private String username;
  private LocalDateTime createdAt;
  
  public static SystemLogDto fromEntity(SystemLog log) {
    SystemLogDto dto = new SystemLogDto();
    dto.setId(log.getId());
    dto.setType(log.getType());
    dto.setModule(log.getModule());
    dto.setOperation(log.getOperation());
    dto.setDetails(log.getDetails());
    dto.setIpAddress(log.getIpAddress());
    dto.setUsername(log.getUsername());
    dto.setCreatedAt(log.getCreatedAt());
    return dto;
  }
}
