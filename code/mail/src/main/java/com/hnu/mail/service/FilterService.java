// FilterService.java
package com.hnu.mail.service;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.hnu.mail.dto.SendMailRequest;
import com.hnu.mail.model.FilterResult;
import com.hnu.mail.model.FilterRule;
import com.hnu.mail.model.User;
import com.hnu.mail.repository.FilterRuleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FilterService {

  private final FilterRuleRepository filterRuleRepository;

  public FilterResult applyFilters(SendMailRequest request, User sender) {
    // 获取所有激活的过滤规则
    List<FilterRule> rules = filterRuleRepository.findByIsActiveTrueOrderByPriorityAsc();
    
    for (FilterRule rule : rules) {
      boolean matched = false;
      
      // 根据过滤类型进行匹配
      switch (rule.getType()) {
        case EMAIL:
          matched = matchPattern(request.getTo(), rule.getPattern());
          break;
        case SUBJECT:
          matched = matchPattern(request.getSubject(), rule.getPattern());
          break;
        case CONTENT:
          matched = matchPattern(request.getContent(), rule.getPattern());
          break;
        case IP_ADDRESS:
          // 这里假设我们可以获取发送者的IP地址
          // 实际实现中需要从请求中获取客户端IP
          String senderIp = "127.0.0.1"; // 占位符，实际应从请求中获取
          matched = matchPattern(senderIp, rule.getPattern());
          break;
      }
      
      // 如果匹配到规则，根据动作返回结果
      if (matched) {
        if (rule.getAction() == FilterRule.Action.BLOCK) {
          return new FilterResult(true, "邮件被系统过滤规则阻止: " + rule.getDescription());
        } else if (rule.getAction() == FilterRule.Action.MOVE_TO_SPAM) {
          // 这里可以返回特殊结果，让调用者知道需要移动到垃圾邮件
          return new FilterResult(false, "move_to_spam");
        } else if (rule.getAction() == FilterRule.Action.MARK_AS_READ) {
          // 返回特殊结果，让调用者知道需要标记为已读
          return new FilterResult(false, "mark_as_read");
        }
      }
    }
    
    // 没有匹配到任何阻止规则
    return new FilterResult(false, "");
  }
  
  /**
   * 匹配模式，支持简单的正则表达式
   */
  private boolean matchPattern(String content, String pattern) {
    if (content == null || pattern == null) {
      return false;
    }
    
    try {
      return Pattern.matches(pattern, content);
    } catch (Exception e) {
      // 如果正则表达式无效，返回不匹配
      return false;
    }
  }
}
