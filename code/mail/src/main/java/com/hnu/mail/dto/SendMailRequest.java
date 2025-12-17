// SendMailRequest.java
package com.hnu.mail.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendMailRequest {
  @NotBlank
  @Email
  private String to;

  @NotBlank
  private String subject;

  @NotBlank
  private String content;

  private String from;
  
  // 原草稿ID，用于发送草稿时更新原草稿状态
  private Long draftId;
}