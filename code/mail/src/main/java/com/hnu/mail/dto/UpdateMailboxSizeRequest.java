// UpdateMailboxSizeRequest.java
package com.hnu.mail.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateMailboxSizeRequest {
    
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @NotNull(message = "邮箱大小不能为空")
    @Min(value = 1, message = "邮箱大小不能小于1MB")
    private Integer mailboxSize;
}