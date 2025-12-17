// MailDto.java
package com.hnu.mail.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MailDto {
  private Long id;
  private String subject;
  private String content;
  private String from;
  private String to;
  private LocalDateTime sentAt;
  private Boolean isRead;
  private Boolean isStarred;
  private String folder;
  private Integer size;
  private List<String> attachments;
}