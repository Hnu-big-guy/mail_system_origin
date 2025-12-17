// StatisticsDto.java
package com.hnu.mail.dto;

import lombok.Data;

@Data
public class StatisticsDto {
  private long totalUsers;
  private long activeUsers;
  private long totalMails;
  private long todayMails;
  private long totalAttachments;
  private long totalStorageUsed;
}
