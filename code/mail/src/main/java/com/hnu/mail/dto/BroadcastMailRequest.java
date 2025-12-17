// BroadcastMailRequest.java
package com.hnu.mail.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class BroadcastMailRequest {
  @NotBlank
  private String subject;

  @NotBlank
  private String content;

  private List<String> recipientGroups; // 用户组
  private List<Long> recipientIds; // 特定用户ID
}