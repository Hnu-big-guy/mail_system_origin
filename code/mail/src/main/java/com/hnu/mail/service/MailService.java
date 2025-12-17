// MailService.java
package com.hnu.mail.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.hnu.mail.dto.MailDto;
import com.hnu.mail.dto.SendMailRequest;
import com.hnu.mail.model.Attachment;
import com.hnu.mail.model.FilterResult;
import com.hnu.mail.model.Mail;
import com.hnu.mail.model.SystemLog;
import com.hnu.mail.model.User;
import com.hnu.mail.repository.MailRepository;
import com.hnu.mail.repository.UserRepository;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {

  private final MailRepository mailRepository;
  private final UserRepository userRepository;
  private final JavaMailSender mailSender;
  private final FilterService filterService;
  private final LogService logService;

  @Value("${app.mail.storage.local-path}")
  private String uploadPath;

  @Transactional
  public Mail sendMail(SendMailRequest request, Long senderId,
                       List<MultipartFile> attachments) throws Exception {
    User sender = userRepository.findById(senderId)
        .orElseThrow(() -> new RuntimeException("发件人不存在"));

    User receiver = userRepository.findByEmail(request.getTo())
        .orElseThrow(() -> new RuntimeException("收件人不存在"));

    // 检查邮箱容量
    checkMailboxCapacity(receiver, request, attachments);

    // 邮件过滤
    FilterResult filterResult = filterService.applyFilters(request, sender);
    if (filterResult.isBlocked()) {
      // 记录邮件被拦截的日志
      logService.createLog(
          SystemLog.LogType.SECURITY,
          "邮件服务",
          "邮件发送拦截",
          String.format("发件人: %s, 收件人: %s, 主题: %s, 原因: %s",
              sender.getEmail(), request.getTo(), request.getSubject(), filterResult.getMessage()),
          "127.0.0.1", // 占位符，实际应从请求中获取
          sender.getUsername()
      );
      
      throw new RuntimeException("邮件被过滤系统拦截: " + filterResult.getMessage());
    }

    // 处理原草稿邮件（如果存在）
    if (request.getDraftId() != null) {
      Mail draftMail = mailRepository.findById(request.getDraftId())
          .orElseThrow(() -> new RuntimeException("草稿邮件不存在"));
      
      // 验证草稿邮件的所有者是当前发送者
      if (!draftMail.getSender().getId().equals(senderId)) {
        throw new RuntimeException("无权操作此草稿邮件");
      }
      
      // 将草稿邮件标记为已删除，或者可以选择将folder改为SENT
      // 这里选择标记为已删除，因为我们已经创建了新的SENT邮件记录
      draftMail.setIsDeleted(true);
      draftMail.setFolder(Mail.MailFolder.TRASH);
      mailRepository.save(draftMail);
    }

    // 创建收件人邮件记录
    Mail receiverMail = new Mail();
    receiverMail.setSubject(request.getSubject());
    receiverMail.setContent(request.getContent());
    receiverMail.setSender(sender);
    receiverMail.setReceiver(receiver);
    receiverMail.setSenderEmail(sender.getEmail());
    receiverMail.setReceiverEmail(receiver.getEmail());
    
    // 根据过滤结果设置收件人邮件状态
    String filterMessage = filterResult.getMessage();
    if ("move_to_spam".equals(filterMessage)) {
      receiverMail.setFolder(Mail.MailFolder.SPAM);
    } else {
      receiverMail.setFolder(Mail.MailFolder.INBOX);
    }
    
    if ("mark_as_read".equals(filterMessage)) {
      receiverMail.setIsRead(true);
    }
    
    receiverMail.setSentAt(LocalDateTime.now());
    receiverMail.setSize(calculateMailSize(request, attachments));

    // 创建发件人邮件记录
    Mail senderMail = new Mail();
    senderMail.setSubject(request.getSubject());
    senderMail.setContent(request.getContent());
    senderMail.setSender(sender);
    senderMail.setReceiver(receiver);
    senderMail.setSenderEmail(sender.getEmail());
    senderMail.setReceiverEmail(receiver.getEmail());
    senderMail.setFolder(Mail.MailFolder.SENT);
    senderMail.setIsRead(false); // 发件人发送后不自动标记为已读
    senderMail.setSentAt(LocalDateTime.now());
    senderMail.setSize(calculateMailSize(request, attachments));

    // 处理附件
    Set<Attachment> attachmentSet = null;
    if (attachments != null && !attachments.isEmpty()) {
      attachmentSet = new HashSet<>();
      for (MultipartFile file : attachments) {
        if (!file.isEmpty()) {
          // 先保存收件人邮件，以便附件关联
          Mail savedReceiverMail = mailRepository.save(receiverMail);
          Attachment attachment = saveAttachment(file, savedReceiverMail);
          attachmentSet.add(attachment);
        }
      }
      receiverMail.setAttachments(attachmentSet);
      senderMail.setAttachments(attachmentSet);
    }

    // 保存邮件记录
    Mail savedReceiverMail = mailRepository.save(receiverMail);
    Mail savedSenderMail = mailRepository.save(senderMail);

    // 使用发件人邮件记录作为返回值
    Mail savedMail = savedSenderMail;

    // 发送实际邮件（异步）
    sendEmailAsync(request, attachments);

    // 更新用户已使用空间
    updateUserMailboxSize(receiver, savedMail.getSize());
    
    // 记录邮件发送成功日志
    logService.createLog(
        SystemLog.LogType.SEND_MAIL,
        "邮件服务",
        "邮件发送",
        String.format("发件人: %s, 收件人: %s, 主题: %s, 大小: %d KB",
            sender.getEmail(), request.getTo(), request.getSubject(), savedMail.getSize()),
        "127.0.0.1", // 占位符，实际应从请求中获取
        sender.getUsername()
    );

    return savedMail;
  }

  @Transactional(readOnly = true)
  public Page<MailDto> getUserMails(Long userId, String folder,
                                    Pageable pageable) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("用户不存在"));

    Mail.MailFolder mailFolder = Mail.MailFolder.valueOf(folder.toUpperCase());

    Page<Mail> mails;
    if (mailFolder == Mail.MailFolder.SENT || mailFolder == Mail.MailFolder.DRAFT) {
      mails = mailRepository.findBySenderAndFolderAndIsDeletedFalse(
          user, mailFolder, pageable);
    } else {
      mails = mailRepository.findByReceiverAndFolderAndIsDeletedFalse(
          user, mailFolder, pageable);
    }

    return mails.map(this::mapToDto);
  }

  @Transactional
  public MailDto readMail(Long mailId, Long userId) {
    Mail mail = mailRepository.findById(mailId)
        .orElseThrow(() -> new RuntimeException("邮件不存在"));

    if (!mail.getReceiver().getId().equals(userId) &&
        !mail.getSender().getId().equals(userId)) {
      throw new RuntimeException("无权访问此邮件");
    }

    // 只有收件人可以将邮件标记为已读
    if (mail.getReceiver().getId().equals(userId) && !mail.getIsRead()) {
      mail.setIsRead(true);
      mailRepository.save(mail);
    }

    return mapToDto(mail);
  }

  @Transactional
  public void deleteMail(Long mailId, Long userId) {
    Mail mail = mailRepository.findById(mailId)
        .orElseThrow(() -> new RuntimeException("邮件不存在"));

    if (!mail.getReceiver().getId().equals(userId) &&
        !mail.getSender().getId().equals(userId)) {
      throw new RuntimeException("无权删除此邮件");
    }

    // 如果邮件已经删除，则不需要再次保存
    if (!mail.getIsDeleted()) {
      mail.setIsDeleted(true);
      mail.setFolder(Mail.MailFolder.TRASH);
      mailRepository.save(mail);
    }
  }

  @Transactional
  public void moveToFolder(Long mailId, String folder, Long userId) {
    Mail mail = mailRepository.findById(mailId)
        .orElseThrow(() -> new RuntimeException("邮件不存在"));

    if (!mail.getReceiver().getId().equals(userId)) {
      throw new RuntimeException("无权操作此邮件");
    }

    mail.setFolder(Mail.MailFolder.valueOf(folder.toUpperCase()));
    mailRepository.save(mail);
  }

  @Value("${spring.mail.username}")
  private String smtpUsername;

  private void sendEmailAsync(SendMailRequest request,
                              List<MultipartFile> attachments) {
    new Thread(() -> {
      try {
        MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(smtpUsername);
        helper.setTo(request.getTo());
        helper.setSubject(request.getSubject());
        helper.setText(request.getContent(), true);

        if (attachments != null) {
          for (MultipartFile file : attachments) {
            if (!file.isEmpty()) {
              helper.addAttachment(
                  file.getOriginalFilename(),
                  file.getResource(),
                  file.getContentType()
              );
            }
          }
        }

        mailSender.send(message);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }).start();
  }

  private Attachment saveAttachment(MultipartFile file, Mail mail) throws IOException {
    String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
    Path path = Paths.get(uploadPath, filename);

    Files.createDirectories(path.getParent());
    Files.copy(file.getInputStream(), path);

    Attachment attachment = new Attachment();
    attachment.setFilename(file.getOriginalFilename());
    attachment.setFileType(file.getContentType());
    attachment.setFileSize(file.getSize());
    attachment.setFilePath(path.toString());
    attachment.setMail(mail);

    return attachment;
  }

  private Integer calculateMailSize(SendMailRequest request,
                                    List<MultipartFile> attachments) {
    try {
      int size = request.getSubject().getBytes("UTF-8").length +
          request.getContent().getBytes("UTF-8").length;

      if (attachments != null) {
        for (MultipartFile file : attachments) {
          size += file.getSize();
        }
      }

      return size / 1024; // 转换为KB
    } catch (Exception e) {
      throw new RuntimeException("计算邮件大小失败", e);
    }
  }

  private void checkMailboxCapacity(User user, SendMailRequest request,
                                    List<MultipartFile> attachments) {
    int newMailSize = calculateMailSize(request, attachments);
    Integer totalSizeByUser = mailRepository.getTotalMailSizeByUser(user);
    int totalSize = (totalSizeByUser != null ? totalSizeByUser : 0) + newMailSize;

    if (totalSize > user.getMailboxSize() * 1024) { // 转换为KB
      throw new RuntimeException("邮箱空间不足");
    }
  }

  private void updateUserMailboxSize(User user, int mailSize) {
    user.setUsedSize(user.getUsedSize() + mailSize);
    userRepository.save(user);
  }

  @Transactional
  public Mail saveDraft(SendMailRequest request, Long senderId, List<MultipartFile> attachments) throws Exception {
    User sender = userRepository.findById(senderId)
        .orElseThrow(() -> new RuntimeException("发件人不存在"));

    // 创建草稿邮件
    Mail draftMail = new Mail();
    draftMail.setSubject(request.getSubject());
    draftMail.setContent(request.getContent());
    draftMail.setSender(sender);
    draftMail.setSenderEmail(sender.getEmail());
    draftMail.setFolder(Mail.MailFolder.DRAFT);
    draftMail.setIsRead(true);
    draftMail.setSentAt(LocalDateTime.now());
    draftMail.setSize(calculateMailSize(request, attachments));

    // 设置收件人信息（如果有）
    if (request.getTo() != null && !request.getTo().isEmpty()) {
      User receiver = userRepository.findByEmail(request.getTo()).orElse(null);
      if (receiver != null) {
        draftMail.setReceiver(receiver);
        draftMail.setReceiverEmail(receiver.getEmail());
      } else {
        draftMail.setReceiverEmail(request.getTo());
      }
    }

    // 保存邮件记录
    Mail savedDraft = mailRepository.save(draftMail);

    // 处理附件
    if (attachments != null && !attachments.isEmpty()) {
      Set<Attachment> attachmentSet = new HashSet<>();
      for (MultipartFile file : attachments) {
        if (!file.isEmpty()) {
          Attachment attachment = saveAttachment(file, savedDraft);
          attachmentSet.add(attachment);
        }
      }
      savedDraft.setAttachments(attachmentSet);
      savedDraft = mailRepository.save(savedDraft);
    }

    // 记录草稿保存日志
    logService.createLog(
        SystemLog.LogType.SAVE_DRAFT,
        "邮件服务",
        "保存草稿",
        String.format("发件人: %s, 主题: %s, 大小: %d KB",
            sender.getEmail(), request.getSubject(), savedDraft.getSize()),
        "127.0.0.1", // 占位符，实际应从请求中获取
        sender.getUsername()
    );

    return savedDraft;
  }

  private MailDto mapToDto(Mail mail) {
    MailDto dto = new MailDto();
    dto.setId(mail.getId());
    dto.setSubject(mail.getSubject());
    dto.setContent(mail.getContent());
    dto.setFrom(mail.getSenderEmail());
    dto.setTo(mail.getReceiverEmail());
    dto.setSentAt(mail.getSentAt());
    dto.setIsRead(mail.getIsRead());
    dto.setIsStarred(mail.getIsStarred());
    dto.setFolder(mail.getFolder().name());
    dto.setSize(mail.getSize());

    if (mail.getAttachments() != null) {
      List<String> attachments = new ArrayList<>();
      for (Attachment attachment : mail.getAttachments()) {
        attachments.add(attachment.getFilename());
      }
      dto.setAttachments(attachments);
    }

    return dto;
  }
}