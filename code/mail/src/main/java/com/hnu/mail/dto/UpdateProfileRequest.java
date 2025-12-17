// UpdateProfileRequest.java
package com.hnu.mail.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
  @Size(max = 50)
  private String nickname;
  
  @Size(max = 20)
  private String phone;
}
