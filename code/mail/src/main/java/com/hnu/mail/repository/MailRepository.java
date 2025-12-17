// MailRepository.java
package com.hnu.mail.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hnu.mail.model.Mail;
import com.hnu.mail.model.User;

@Repository
public interface MailRepository extends JpaRepository<Mail, Long> {

  Page<Mail> findByReceiverAndFolderAndIsDeletedFalse(
      User receiver, Mail.MailFolder folder, Pageable pageable);

  Page<Mail> findBySenderAndFolderAndIsDeletedFalse(
      User sender, Mail.MailFolder folder, Pageable pageable);

  List<Mail> findByReceiverAndIsReadFalse(User receiver);

  @Query("SELECT m FROM Mail m WHERE " +
      "(m.receiver = :user AND m.folder = :folder) OR " +
      "(m.sender = :user AND m.folder = 'SENT') " +
      "ORDER BY m.sentAt DESC")
  Page<Mail> findUserMails(@Param("user") User user,
                           @Param("folder") Mail.MailFolder folder,
                           Pageable pageable);

  @Query("SELECT SUM(m.size) FROM Mail m WHERE m.receiver = :user")
  Integer getTotalMailSizeByUser(@Param("user") User user);

  Long countByReceiverAndIsReadFalse(User receiver);

  List<Mail> findBySentAtBetween(LocalDateTime start, LocalDateTime end);

  Long countBySentAtAfter(LocalDateTime time);
  
  // 为POP3服务器添加的方法，根据收件人用户名查找邮件
  @Query("SELECT m FROM Mail m WHERE m.receiver.username = :username AND m.folder = 'INBOX' AND m.isDeleted = false")
  List<Mail> findByRecipient(@Param("username") String username);
} 