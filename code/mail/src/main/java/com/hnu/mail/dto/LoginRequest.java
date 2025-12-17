// LoginRequest.java
package com.hnu.mail.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class LoginRequest {
  @NotBlank
  private String username;

  @NotBlank
  private String password;
}



