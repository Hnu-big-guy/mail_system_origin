// Pop3Service.java
package com.hnu.mail.service;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.mail.BodyPart;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.InternetAddress;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class Pop3Service {

  @Value("${app.mail.pop3.host}")
  private String pop3Host;

  @Value("${app.mail.pop3.port}")
  private int pop3Port;

  @Value("${app.mail.pop3.ssl-enabled}")
  private boolean sslEnabled;

  @Value("${spring.mail.username}")
  private String username;

  @Value("${spring.mail.password}")
  private String password;

  // POP3服务状态
  private boolean serviceEnabled = true;

  /**
   * 定时从外部POP3服务器拉取邮件
   * 每小时执行一次
   */
  @Scheduled(fixedRate = 3600000) // 1小时
  public void fetchExternalEmails() {
    if (!serviceEnabled) {
      log.info("POP3服务已停止，跳过邮件拉取");
      return;
    }
    log.info("开始从外部POP3服务器拉取邮件...");

    Store store = null;
    Folder folder = null;

    try {
      // 配置POP3连接
      Properties props = new Properties();
      props.put("mail.store.protocol", "pop3");
      props.put("mail.pop3.host", pop3Host);
      props.put("mail.pop3.port", pop3Port);
      props.put("mail.pop3.ssl.enable", sslEnabled);

      Session session = Session.getDefaultInstance(props, null);
      store = session.getStore("pop3");
      store.connect(pop3Host, username, password);

      folder = store.getFolder("INBOX");
      folder.open(Folder.READ_WRITE);

      // 获取未读邮件
      Message[] messages = folder.getMessages();

      for (Message message : messages) {
        if (!message.getFlags().contains(Flags.Flag.SEEN)) {
          processMessage(message);
          // 标记为已读
          message.setFlag(Flags.Flag.SEEN, true);
        }
      }

      log.info("成功拉取 {} 封新邮件", messages.length);

    } catch (Exception e) {
      log.error("拉取邮件失败: ", e);
    } finally {
      try {
        if (folder != null && folder.isOpen()) {
          folder.close(true);
        }
        if (store != null) {
          store.close();
        }
      } catch (MessagingException e) {
        log.error("关闭POP3连接失败: ", e);
      }
    }
  }

  private void processMessage(Message message) throws Exception {
    String from = InternetAddress.toString(message.getFrom());
    String subject = message.getSubject();
    String content = getTextFromMessage(message);
    String to = InternetAddress.toString(message.getRecipients(
        Message.RecipientType.TO));

    // 保存到本地数据库
    saveToLocalDatabase(from, to, subject, content);
  }

  private String getTextFromMessage(Message message) throws Exception {
    if (message.isMimeType("text/plain")) {
      return message.getContent().toString();
    } else if (message.isMimeType("text/html")) {
      return message.getContent().toString();
    } else if (message.isMimeType("multipart/*")) {
      Multipart multipart = (Multipart) message.getContent();
      return getTextFromMimeMultipart(multipart);
    }
    return "";
  }

  private String getTextFromMimeMultipart(Multipart multipart) throws Exception {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < multipart.getCount(); i++) {
      BodyPart bodyPart = multipart.getBodyPart(i);
      if (bodyPart.isMimeType("text/plain")) {
        result.append(bodyPart.getContent());
        break;
      } else if (bodyPart.isMimeType("text/html")) {
        result.append(bodyPart.getContent());
      } else if (bodyPart.getContent() instanceof Multipart) {
        result.append(getTextFromMimeMultipart((Multipart) bodyPart.getContent()));
      }
    }
    return result.toString();
  }

  private void saveToLocalDatabase(String from, String to,
                                   String subject, String content) {
    // TODO: 实现邮件保存到本地数据库的逻辑
    log.info("保存邮件: 发件人={}, 收件人={}, 主题={}", from, to, subject);
  }

  /**
   * 启动POP3服务
   */
  public void start() {
    serviceEnabled = true;
    log.info("POP3服务已启动");
  }

  /**
   * 停止POP3服务
   */
  public void stop() {
    serviceEnabled = false;
    log.info("POP3服务已停止");
  }
}