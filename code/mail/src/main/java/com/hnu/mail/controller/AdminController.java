// AdminController.java
package com.hnu.mail.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hnu.mail.dto.AdminCreateUserRequest;
import com.hnu.mail.dto.ApiResponse;
import com.hnu.mail.dto.BroadcastMailRequest;
import com.hnu.mail.dto.FilterRuleDto;
import com.hnu.mail.dto.ResetPasswordRequest;
import com.hnu.mail.dto.StatisticsDto;
import com.hnu.mail.dto.SystemLogDto;
import com.hnu.mail.dto.UpdateMailboxSizeRequest;
import com.hnu.mail.model.FilterRule;
import com.hnu.mail.model.User;
import com.hnu.mail.security.UserPrincipal;
import com.hnu.mail.model.SystemConfig;
import com.hnu.mail.service.AdminService;
import com.hnu.mail.service.MailService;
import com.hnu.mail.service.LogService;
import com.hnu.mail.service.ServiceManager;
import com.hnu.mail.service.SystemConfigService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

  private final AdminService adminService;
  private final MailService mailService;
  private final LogService logService;
  private final SystemConfigService systemConfigService;

  @GetMapping("/users")
  public ResponseEntity<?> getAllUsers(
      @PageableDefault(size = 20) Pageable pageable) {

    Page<User> users = adminService.getAllUsers(pageable);
    return ResponseEntity.ok(users);
  }

  @PostMapping("/users")
  public ResponseEntity<?> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
    try {
      User user = adminService.createUser(request);
      return ResponseEntity.ok(new ApiResponse(true, "用户创建成功"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PutMapping("/users/{id}/status")
  public ResponseEntity<?> updateUserStatus(
      @PathVariable Long id,
      @RequestParam String status) {

    try {
      adminService.updateUserStatus(id, status);
      return ResponseEntity.ok(new ApiResponse(true, "用户状态已更新"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PutMapping("/users/{id}/role")
  public ResponseEntity<?> updateUserRole(
      @PathVariable Long id,
      @RequestParam String role) {

    try {
      adminService.updateUserRole(id, role);
      return ResponseEntity.ok(new ApiResponse(true, "用户角色已更新"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @DeleteMapping("/users/{id}")
  public ResponseEntity<?> deleteUser(
      @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    try {
      // 检查是否删除自己
      if (id.equals(userPrincipal.getId())) {
        return ResponseEntity.badRequest()
            .body(new ApiResponse(false, "管理员不能删除自己"));
      }
      adminService.deleteUser(id);
      return ResponseEntity.ok(new ApiResponse(true, "用户已删除"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PostMapping("/mail/broadcast")
  public ResponseEntity<?> broadcastMail(
      @Valid @RequestBody BroadcastMailRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    try {
      adminService.broadcastMail(request, userPrincipal.getId());
      return ResponseEntity.ok(new ApiResponse(true, "群发邮件任务已启动"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @GetMapping("/statistics")
  public ResponseEntity<?> getStatistics() {
    StatisticsDto statistics = adminService.getStatistics();
    return ResponseEntity.ok(statistics);
  }

  @GetMapping("/logs")
  public ResponseEntity<?> getSystemLogs(
      @RequestParam(required = false) String type,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate,
      @PageableDefault(size = 50) Pageable pageable) {

    Page<SystemLogDto> logs = adminService.getSystemLogs(
        type, startDate, endDate, pageable);
    return ResponseEntity.ok(logs);
  }

  @GetMapping("/logs/smtp")
  public ResponseEntity<?> getSmtpLogs(
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate,
      @PageableDefault(size = 50) Pageable pageable) {

    // SMTP日志主要是发送邮件相关的日志
    Page<SystemLogDto> logs = adminService.getSystemLogs(
        "SEND_MAIL", startDate, endDate, pageable);
    return ResponseEntity.ok(logs);
  }

  @GetMapping("/logs/pop3")
  public ResponseEntity<?> getPop3Logs(
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate,
      @PageableDefault(size = 50) Pageable pageable) {

    // POP3日志主要是接收邮件相关的日志
    Page<SystemLogDto> logs = adminService.getSystemLogs(
        "RECEIVE_MAIL", startDate, endDate, pageable);
    return ResponseEntity.ok(logs);
  }

  @PutMapping("/users/{id}/mailbox-size")
  public ResponseEntity<?> updateMailboxSize(
      @PathVariable Long id,
      @Valid @RequestBody UpdateMailboxSizeRequest request) {

    try {
      adminService.updateMailboxSize(id, request.getMailboxSize());
      return ResponseEntity.ok(new ApiResponse(true, "邮箱大小已更新"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PutMapping("/users/{id}/reset-password")
  public ResponseEntity<?> resetPassword(
      @PathVariable Long id,
      @Valid @RequestBody ResetPasswordRequest request) {

    try {
      adminService.resetPassword(id, request.getNewPassword());
      return ResponseEntity.ok(new ApiResponse(true, "密码已重置"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, e.getMessage()));
    }
  }

  // 过滤规则管理接口
  @GetMapping("/filter-rules")
  public ResponseEntity<?> getAllFilterRules() {
    try {
      return ResponseEntity.ok(adminService.getAllFilterRules());
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @GetMapping("/filter-rules/{id}")
  public ResponseEntity<?> getFilterRuleById(@PathVariable Long id) {
    try {
      FilterRule rule = adminService.getFilterRuleById(id);
      return ResponseEntity.ok(FilterRuleDto.fromEntity(rule));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PostMapping("/filter-rules")
  public ResponseEntity<?> createFilterRule(@Valid @RequestBody FilterRuleDto request) {
    try {
      FilterRule rule = adminService.createFilterRule(request.toEntity());
      return ResponseEntity.ok(FilterRuleDto.fromEntity(rule));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PostMapping("/filters/email")
  public ResponseEntity<?> createEmailFilterRule(@Valid @RequestBody FilterRuleDto request) {
    try {
      // 创建邮箱过滤规则，自动设置类型为EMAIL
      FilterRule rule = request.toEntity();
      rule.setType(FilterRule.FilterType.EMAIL);
      FilterRule createdRule = adminService.createFilterRule(rule);
      return ResponseEntity.ok(FilterRuleDto.fromEntity(createdRule));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PostMapping("/filters/ip")
  public ResponseEntity<?> createIpFilterRule(@Valid @RequestBody FilterRuleDto request) {
    try {
      // 创建IP过滤规则，自动设置类型为IP_ADDRESS
      FilterRule rule = request.toEntity();
      rule.setType(FilterRule.FilterType.IP_ADDRESS);
      FilterRule createdRule = adminService.createFilterRule(rule);
      return ResponseEntity.ok(FilterRuleDto.fromEntity(createdRule));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PutMapping("/filter-rules/{id}")
  public ResponseEntity<?> updateFilterRule(
      @PathVariable Long id,
      @Valid @RequestBody FilterRuleDto request) {

    try {
      FilterRule updatedRule = adminService.updateFilterRule(id, request.toEntity());
      return ResponseEntity.ok(FilterRuleDto.fromEntity(updatedRule));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @DeleteMapping("/filter-rules/{id}")
  public ResponseEntity<?> deleteFilterRule(@PathVariable Long id) {
    try {
      adminService.deleteFilterRule(id);
      return ResponseEntity.ok(new ApiResponse(true, "过滤规则已删除"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @DeleteMapping("/logs/old")
  public ResponseEntity<?> deleteOldLogs(@RequestParam String beforeDate) {
    try {
      java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(beforeDate);
      logService.deleteOldLogs(dateTime);
      return ResponseEntity.ok(new ApiResponse(true, "旧日志已删除"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, "日期格式错误或删除失败: " + e.getMessage()));
    }
  }

  @DeleteMapping("/logs/all")
  public ResponseEntity<?> clearAllLogs() {
    try {
      logService.clearAllLogs();
      return ResponseEntity.ok(new ApiResponse(true, "所有日志已清空"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, "清空日志失败: " + e.getMessage()));
    }
  }

  // 系统参数设置接口
  @GetMapping("/configs")
  public ResponseEntity<?> getAllConfigs() {
    try {
      List<SystemConfig> configs = systemConfigService.getAllConfigs();
      return ResponseEntity.ok(configs);
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, "获取系统配置失败: " + e.getMessage()));
    }
  }

  @GetMapping("/configs/{key}")
  public ResponseEntity<?> getConfigByKey(@PathVariable String key) {
    try {
      return systemConfigService.getConfigByKey(key)
          .map(ResponseEntity::ok)
          .orElse(ResponseEntity.notFound().build());
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, "获取系统配置失败: " + e.getMessage()));
    }
  }

  @PutMapping("/configs/{key}")
  public ResponseEntity<?> updateConfig(
      @PathVariable String key,
      @RequestParam String value,
      @RequestParam(required = false) String description,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    try {
      SystemConfig config = systemConfigService.updateConfig(
          key, value, description, userPrincipal.getUsername());
      return ResponseEntity.ok(config);
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, "更新系统配置失败: " + e.getMessage()));
    }
  }

  @DeleteMapping("/configs/{key}")
  public ResponseEntity<?> deleteConfig(
      @PathVariable String key,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    try {
      systemConfigService.deleteConfig(key, userPrincipal.getUsername());
      return ResponseEntity.ok(new ApiResponse(true, "配置已删除"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, "删除配置失败: " + e.getMessage()));
    }
  }

  // 服务管理接口
  @Autowired
  private ServiceManager serviceManager;

  @GetMapping("/services/status")
  public ResponseEntity<?> getServicesStatus() {
    Map<String, Boolean> statusMap = new HashMap<>();
    statusMap.put("smtpEnabled", serviceManager.isSmtpServiceEnabled());
    statusMap.put("pop3Enabled", serviceManager.isPop3ServiceEnabled());
    return ResponseEntity.ok(statusMap);
  }

  @PostMapping("/services/smtp/start")
  public ResponseEntity<?> startSmtpService() {
    try {
      serviceManager.startSmtpService();
      return ResponseEntity.ok(new ApiResponse(true, "SMTP服务已启动"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, "启动SMTP服务失败: " + e.getMessage()));
    }
  }

  @PostMapping("/services/smtp/stop")
  public ResponseEntity<?> stopSmtpService() {
    try {
      serviceManager.stopSmtpService();
      return ResponseEntity.ok(new ApiResponse(true, "SMTP服务已停止"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, "停止SMTP服务失败: " + e.getMessage()));
    }
  }

  @PostMapping("/services/smtp/restart")
  public ResponseEntity<?> restartSmtpService() {
    try {
      serviceManager.restartSmtpService();
      return ResponseEntity.ok(new ApiResponse(true, "SMTP服务已重启"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, "重启SMTP服务失败: " + e.getMessage()));
    }
  }

  @PostMapping("/services/pop3/start")
  public ResponseEntity<?> startPop3Service() {
    try {
      serviceManager.startPop3Service();
      return ResponseEntity.ok(new ApiResponse(true, "POP3服务已启动"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, "启动POP3服务失败: " + e.getMessage()));
    }
  }

  @PostMapping("/services/pop3/stop")
  public ResponseEntity<?> stopPop3Service() {
    try {
      serviceManager.stopPop3Service();
      return ResponseEntity.ok(new ApiResponse(true, "POP3服务已停止"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, "停止POP3服务失败: " + e.getMessage()));
    }
  }

  @PostMapping("/services/pop3/restart")
  public ResponseEntity<?> restartPop3Service() {
    try {
      serviceManager.restartPop3Service();
      return ResponseEntity.ok(new ApiResponse(true, "POP3服务已重启"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, "重启POP3服务失败: " + e.getMessage()));
    }
  }
}