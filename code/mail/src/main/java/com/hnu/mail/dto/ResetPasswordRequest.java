// ResetPasswordRequest.java
package com.hnu.mail.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 120, message = "密码长度必须在6到120个字符之间")
    private String newPassword;
}