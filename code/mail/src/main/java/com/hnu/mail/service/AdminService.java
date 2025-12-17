// AdminService.java
package com.hnu.mail.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hnu.mail.dto.AdminCreateUserRequest;
import com.hnu.mail.dto.BroadcastMailRequest;
import com.hnu.mail.dto.SendMailRequest;
import com.hnu.mail.dto.StatisticsDto;
import com.hnu.mail.dto.SystemLogDto;
import com.hnu.mail.model.FilterRule;
import com.hnu.mail.model.SystemLog;
import com.hnu.mail.model.User;
import com.hnu.mail.repository.FilterRuleRepository;
import com.hnu.mail.repository.MailRepository;
import com.hnu.mail.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

  private final UserRepository userRepository;
  private final MailRepository mailRepository;
  private final MailService mailService;
  private final PasswordEncoder passwordEncoder;
  private final FilterRuleRepository filterRuleRepository;
  private final LogService logService;

  @Transactional(readOnly = true)
  public Page<User> getAllUsers(Pageable pageable) {
    return userRepository.findAll(pageable);
  }

  @Transactional
  public User createUser(AdminCreateUserRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new RuntimeException("用户名已存在");
    }

    if (userRepository.existsByEmail(request.getEmail())) {
      throw new RuntimeException("邮箱已被注册");
    }

    User user = new User();
    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setNickname(request.getNickname());
    user.setPhone(request.getPhone());
    user.setRole(request.getRole() != null ? request.getRole() : User.UserRole.USER);
    user.setStatus(request.getStatus() != null ? request.getStatus() : User.UserStatus.ACTIVE);

    return userRepository.save(user);
  }

  @Transactional
  public void updateUserStatus(Long id, String status) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("用户不存在"));

    user.setStatus(User.UserStatus.valueOf(status.toUpperCase()));
    userRepository.save(user);
  }

  @Transactional
  public void updateUserRole(Long id, String role) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("用户不存在"));

    user.setRole(User.UserRole.valueOf(role.toUpperCase()));
    userRepository.save(user);
  }

  @Transactional
  public void deleteUser(Long id) {
    userRepository.deleteById(id);
  }

  @Transactional
  public void broadcastMail(BroadcastMailRequest request, Long adminId) {
    // 获取管理员信息作为发件人
    User admin = userRepository.findById(adminId)
        .orElseThrow(() -> new RuntimeException("管理员不存在"));
    
    List<Long> allUserIds = userRepository.findAllUserIds();
    
    // 批量发送邮件
    for (Long userId : allUserIds) {
      try {
        // 获取用户信息作为收件人
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        SendMailRequest mailRequest = new SendMailRequest();
        mailRequest.setTo(user.getEmail());
        mailRequest.setSubject(request.getSubject());
        mailRequest.setContent(request.getContent());
        mailRequest.setFrom(admin.getEmail());
        
        mailService.sendMail(mailRequest, adminId, new ArrayList<>());
      } catch (Exception e) {
        // 记录发送失败的用户，但继续发送给其他用户
        e.printStackTrace();
      }
    }
  }

  @Transactional(readOnly = true)
  public StatisticsDto getStatistics() {
    StatisticsDto statistics = new StatisticsDto();
    
    statistics.setTotalUsers(userRepository.count());
    statistics.setActiveUsers(userRepository.countByStatus(User.UserStatus.ACTIVE));
    statistics.setTotalMails(mailRepository.count());
    statistics.setTodayMails(mailRepository.countBySentAtAfter(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0)));
    statistics.setTotalAttachments(0); // 需要实现附件统计功能
    statistics.setTotalStorageUsed(0); // 需要实现存储使用统计功能
    
    return statistics;
  }

  @Transactional(readOnly = true)
  public Page<SystemLogDto> getSystemLogs(String type, String startDate, String endDate, Pageable pageable) {
    Page<SystemLog> logs;
    
    if (type != null) {
      SystemLog.LogType logType = SystemLog.LogType.valueOf(type.toUpperCase());
      logs = logService.getLogsByType(logType, pageable);
    } else {
      logs = logService.getAllLogs(pageable);
    }
    
    return logs.map(SystemLogDto::fromEntity);
  }

  @Transactional
  public void updateMailboxSize(Long userId, Integer mailboxSize) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("用户不存在"));
    
    user.setMailboxSize(mailboxSize);
    userRepository.save(user);
  }

  @Transactional
  public void resetPassword(Long userId, String newPassword) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("用户不存在"));
    
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
  }

  @Transactional(readOnly = true)
  public List<FilterRule> getAllFilterRules() {
    return filterRuleRepository.findAll();
  }

  @Transactional(readOnly = true)
  public FilterRule getFilterRuleById(Long id) {
    return filterRuleRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("过滤规则不存在"));
  }

  @Transactional
  public FilterRule createFilterRule(FilterRule filterRule) {
    return filterRuleRepository.save(filterRule);
  }

  @Transactional
  public FilterRule updateFilterRule(Long id, FilterRule updatedRule) {
    FilterRule existingRule = filterRuleRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("过滤规则不存在"));
    
    existingRule.setType(updatedRule.getType());
    existingRule.setPattern(updatedRule.getPattern());
    existingRule.setAction(updatedRule.getAction());
    existingRule.setIsActive(updatedRule.getIsActive());
    existingRule.setPriority(updatedRule.getPriority());
    existingRule.setDescription(updatedRule.getDescription());
    
    return filterRuleRepository.save(existingRule);
  }

  @Transactional
  public void deleteFilterRule(Long id) {
    filterRuleRepository.deleteById(id);
  }
}
