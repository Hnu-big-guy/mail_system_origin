// Mail.java
package com.hnu.mail.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "mails")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Size(max = 200)
  private String subject;

  @Lob
  @Column(columnDefinition = "TEXT")
  private String content;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_id")
  private User sender;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_id")
  private User receiver;

  @Size(max = 100)
  private String senderEmail;

  @Size(max = 100)
  private String receiverEmail;
  
  // 为了兼容POP3和SMTP服务器代码的方法别名
  public User getRecipient() {
    return receiver;
  }
  
  public void setRecipient(User recipient) {
    this.receiver = recipient;
  }
  
  public LocalDateTime getSentDate() {
    return sentAt;
  }
  
  public void setSentDate(LocalDateTime sentDate) {
    this.sentAt = sentDate;
  }

  private Boolean isRead = false;

  private Boolean isStarred = false;

  private Boolean isDeleted = false;

  private Boolean isDraft = false;

  private LocalDateTime sentAt = LocalDateTime.now();
  
  // 添加一个接受字符串类型recipient的setter方法，用于SMTP服务器
  public void setRecipient(String username) {
    // 这个方法主要用于SMTP服务器接受邮件时设置收件人
    // 实际的User对象设置需要在服务层处理
  }

  private LocalDateTime receivedAt;

  private Integer size; // 邮件大小(KB)

  @OneToMany(mappedBy = "mail", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Attachment> attachments = new HashSet<>();

  @Enumerated(EnumType.STRING)
  private MailFolder folder = MailFolder.INBOX;

  public enum MailFolder {
    INBOX, SENT, DRAFT, TRASH, SPAM
  }
}