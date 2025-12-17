package com.hnu.mail.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.multipart.MultipartFile;

import com.hnu.mail.dto.SendMailRequest;
import com.hnu.mail.model.FilterResult;
import com.hnu.mail.model.Mail;
import com.hnu.mail.model.User;
import com.hnu.mail.repository.MailRepository;
import com.hnu.mail.repository.UserRepository;

import jakarta.mail.internet.MimeMessage;

class MailServiceTest {

    @Mock
    private MailRepository mailRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private FilterService filterService;

    @Mock
    private LogService logService;
    
    @Mock
    private MimeMessage mimeMessage;

    @Mock
    private MultipartFile attachmentFile;

    @Mock
    private MimeMessageHelper mimeMessageHelper; // 添加这个mock

    @InjectMocks
    private MailService mailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendMail_Success() throws Exception, InterruptedException {
        // 准备测试数据
        SendMailRequest request = new SendMailRequest();
        request.setTo("receiver@example.com");
        request.setFrom("sender@example.com");
        request.setSubject("测试邮件");
        request.setContent("这是一封测试邮件");
        Long senderId = 1L;
        List<MultipartFile> attachments = new ArrayList<>();

        // 模拟依赖行为
        User sender = new User();
        sender.setId(senderId);
        sender.setEmail("sender@example.com");
        sender.setUsername("sender");

        User receiver = new User();
        receiver.setId(2L);
        receiver.setEmail("receiver@example.com");
        receiver.setUsername("receiver");

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findByEmail("receiver@example.com")).thenReturn(Optional.of(receiver));

        FilterResult filterResult = new FilterResult();
        filterResult.setBlocked(false);
        when(filterService.applyFilters(request, sender)).thenReturn(filterResult);

        Mail savedMail = new Mail();
        savedMail.setId(1L);
        savedMail.setSubject("测试邮件");
        savedMail.setContent("这是一封测试邮件");
        savedMail.setSender(sender);
        savedMail.setReceiver(receiver);
        savedMail.setSenderEmail(sender.getEmail());
        savedMail.setReceiverEmail(receiver.getEmail());
        savedMail.setFolder(Mail.MailFolder.INBOX);
        savedMail.setSentAt(LocalDateTime.now());
        savedMail.setSize(1024);

        when(mailRepository.save(any(Mail.class))).thenReturn(savedMail);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // 执行测试
        Mail result = mailService.sendMail(request, senderId, attachments);
        
        // 等待异步线程执行完成
        Thread.sleep(500);

        // 验证结果
        assertNotNull(result);
        assertEquals("测试邮件", result.getSubject());
        assertEquals("这是一封测试邮件", result.getContent());
        assertEquals(sender, result.getSender());
        assertEquals(receiver, result.getReceiver());

        // 验证依赖方法调用
        verify(userRepository, times(1)).findById(senderId);
        verify(userRepository, times(1)).findByEmail("receiver@example.com");
        verify(filterService, times(1)).applyFilters(request, sender);
        verify(mailRepository, times(1)).save(any(Mail.class));
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendMail_ReceiverNotFound() {
        // 准备测试数据
        SendMailRequest request = new SendMailRequest();
        request.setTo("nonexistent@example.com");
        Long senderId = 1L;

        // 模拟依赖行为
        User sender = new User();
        sender.setId(senderId);
        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // 执行测试并验证异常
        Exception exception = assertThrows(Exception.class, () -> {
            mailService.sendMail(request, senderId, new ArrayList<>());
        });

        assertTrue(exception.getMessage().contains("收件人不存在"));
        verify(userRepository, times(1)).findById(senderId);
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    void testSendMail_FilterBlocked() throws Exception {
        // 准备测试数据
        SendMailRequest request = new SendMailRequest();
        request.setTo("receiver@example.com");
        request.setSubject("垃圾邮件");
        request.setContent("这是垃圾邮件");
        Long senderId = 1L;

        // 模拟依赖行为
        User sender = new User();
        sender.setId(senderId);
        sender.setEmail("sender@example.com");

        User receiver = new User();
        receiver.setId(2L);
        receiver.setEmail("receiver@example.com");

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findByEmail("receiver@example.com")).thenReturn(Optional.of(receiver));

        FilterResult filterResult = new FilterResult();
        filterResult.setBlocked(true);
        filterResult.setMessage("垃圾邮件");
        when(filterService.applyFilters(request, sender)).thenReturn(filterResult);

        // 执行测试并验证异常
        Exception exception = assertThrows(Exception.class, () -> {
            mailService.sendMail(request, senderId, new ArrayList<>());
        });

        assertTrue(exception.getMessage().contains("邮件被过滤系统拦截") ||
            exception.getMessage().contains("垃圾邮件"));
        verify(filterService, times(1)).applyFilters(request, sender);
        // 确保邮件没有被保存
        verify(mailRepository, times(0)).save(any(Mail.class));
    }

    // 修复deleteMail测试方法
    @Test
    void testDeleteMail_Success_Sender() {
        // 准备测试数据
        Long mailId = 1L;
        Long userId = 1L;

        // 模拟依赖行为
        User user = new User();
        user.setId(userId);
        user.setEmail("sender@example.com");

        User receiver = new User(); // 添加receiver
        receiver.setId(2L);
        receiver.setEmail("receiver@example.com");

        Mail mail = new Mail();
        mail.setId(mailId);
        mail.setSender(user);
        mail.setReceiver(receiver); // 设置receiver
        mail.setIsDeleted(false);
        mail.setFolder(Mail.MailFolder.INBOX);

        when(mailRepository.findById(mailId)).thenReturn(Optional.of(mail));
        when(mailRepository.save(any(Mail.class))).thenReturn(mail);

        // 执行测试
        assertDoesNotThrow(() -> mailService.deleteMail(mailId, userId));

        // 验证结果
        assertTrue(mail.getIsDeleted());
        assertEquals(Mail.MailFolder.TRASH, mail.getFolder());

        // 验证依赖方法调用
        verify(mailRepository, times(1)).findById(mailId);
        verify(mailRepository, times(1)).save(mail);
    }

    @Test
    void testDeleteMail_Success_Receiver() {
        // 准备测试数据
        Long mailId = 1L;
        Long userId = 2L;

        // 模拟依赖行为
        User sender = new User();
        sender.setId(1L);
        sender.setEmail("sender@example.com");

        User receiver = new User();
        receiver.setId(userId);
        receiver.setEmail("receiver@example.com");

        Mail mail = new Mail();
        mail.setId(mailId);
        mail.setSender(sender);
        mail.setReceiver(receiver);
        mail.setIsDeleted(false);
        mail.setFolder(Mail.MailFolder.INBOX);

        when(mailRepository.findById(mailId)).thenReturn(Optional.of(mail));
        when(mailRepository.save(any(Mail.class))).thenReturn(mail);

        // 执行测试
        assertDoesNotThrow(() -> mailService.deleteMail(mailId, userId));

        // 验证结果
        assertTrue(mail.getIsDeleted());
        assertEquals(Mail.MailFolder.TRASH, mail.getFolder());

        // 验证依赖方法调用
        verify(mailRepository, times(1)).findById(mailId);
        verify(mailRepository, times(1)).save(mail);
    }

    // 添加一个测试来验证邮件删除逻辑
    @Test
    void testDeleteMail_AlreadyDeleted() {
        // 准备测试数据
        Long mailId = 1L;
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        User receiver = new User();
        receiver.setId(2L);

        Mail mail = new Mail();
        mail.setId(mailId);
        mail.setSender(user);
        mail.setReceiver(receiver);
        mail.setIsDeleted(true); // 已经删除

        when(mailRepository.findById(mailId)).thenReturn(Optional.of(mail));

        // 执行测试 - 已经删除的邮件再次删除应该不会出错
        assertDoesNotThrow(() -> mailService.deleteMail(mailId, userId));

        // 验证邮件状态没有改变
        assertTrue(mail.getIsDeleted());
        verify(mailRepository, times(1)).findById(mailId);
        verify(mailRepository, times(0)).save(any(Mail.class)); // 不应该保存
    }
}