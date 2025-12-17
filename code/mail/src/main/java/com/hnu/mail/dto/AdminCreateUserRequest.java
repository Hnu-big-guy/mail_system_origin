// AdminCreateUserRequest.java
package com.hnu.mail.dto;

import com.hnu.mail.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminCreateUserRequest {
  @NotBlank
  @Size(max = 50)
  private String username;
  
  @NotBlank
  @Size(max = 100)
  @Email
  private String email;
  
  @NotBlank
  @Size(min = 6, max = 120)
  private String password;
  
  @Size(max = 50)
  private String nickname;
  
  @Size(max = 20)
  private String phone;
  
  private User.UserRole role;
  
  private User.UserStatus status;
}
