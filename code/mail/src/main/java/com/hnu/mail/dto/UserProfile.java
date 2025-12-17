// UserProfile.java
package com.hnu.mail.dto;

import com.hnu.mail.model.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfile {
  private Long id;
  private String username;
  private String email;
  private String nickname;
  private String phone;
  private User.UserRole role;
  private User.UserStatus status;
  private Integer mailboxSize;
  private Integer usedSize;
  private LocalDateTime lastLoginTime;
  private LocalDateTime createdAt;
}
