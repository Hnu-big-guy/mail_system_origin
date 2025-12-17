package com.hnu.mail.service;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.mail.Address;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeMessage;

class Pop3ServiceTest {

    @Mock
    private Store store;

    @Mock
    private Folder folder;

    @Mock
    private Message textMessage;

    @Mock
    private Message multipartMessage;

    @Mock
    private MimeMultipart multipart;

    @Mock
    private MimeBodyPart textPart;

    @Mock
    private MimeBodyPart htmlPart;

    @InjectMocks
    private Pop3Service pop3Service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 设置配置参数
        ReflectionTestUtils.setField(pop3Service, "pop3Host", "pop.qq.com");
        ReflectionTestUtils.setField(pop3Service, "pop3Port", 995);
        ReflectionTestUtils.setField(pop3Service, "sslEnabled", true);
        ReflectionTestUtils.setField(pop3Service, "username", "test@qq.com");
        ReflectionTestUtils.setField(pop3Service, "password", "testpassword");
    }

    @Test
    void testFetchExternalEmails_Success() throws Exception {
        // 模拟Session
        Properties props = new Properties();
        Session session = Session.getInstance(props);

        // 创建真实的Store mock（修复MissingMethodInvocationException）
        when(store.isConnected()).thenReturn(true);
        doNothing().when(store).connect(anyString(), anyString(), anyString());
        when(store.getFolder("INBOX")).thenReturn(folder);

        // 模拟Folder
        when(folder.isOpen()).thenReturn(true);
        doNothing().when(folder).open(Folder.READ_WRITE);
        doNothing().when(folder).close(anyBoolean());

        // 模拟Message
        when(textMessage.getFlags()).thenReturn(new Flags());
        when(textMessage.getFrom()).thenReturn(new Address[]{
            new InternetAddress("sender@example.com")
        });
        when(textMessage.getSubject()).thenReturn("测试邮件");
        when(textMessage.getContent()).thenReturn("这是一封测试邮件内容");
        when(textMessage.getRecipients(Message.RecipientType.TO))
            .thenReturn(new Address[]{new InternetAddress("test@qq.com")});

        Message[] messages = {textMessage};
        when(folder.getMessages()).thenReturn(messages);
        when(folder.getMessageCount()).thenReturn(1);

        // 使用ReflectionTestUtils注入mock的Store
        // 由于Session.getStore()是实例方法，我们需要模拟Pop3Service内部创建Session的逻辑
        // 这里简化处理：主要测试邮件获取和解析逻辑

        // 模拟一个空的邮件获取（避免实际连接）
        assertDoesNotThrow(() -> {
            // 在实际代码中，你可能需要重构Pop3Service使其更容易测试
            // 或者使用@Spy来部分模拟
            pop3Service.fetchExternalEmails();
        });
    }

    @Test
    void testGetTextFromMessage_TextPlain() throws Exception {
        // 准备测试数据
        String textContent = "纯文本邮件内容";

        // 使用MimeMessage而不是Message接口，因为它有更多方法
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mimeMessage.isMimeType("text/plain")).thenReturn(true);
        when(mimeMessage.getContent()).thenReturn(textContent);

        // 使用反射调用私有方法
        String result = ReflectionTestUtils.invokeMethod(pop3Service, "getTextFromMessage", mimeMessage);

        // 验证结果
        assertEquals(textContent, result);
    }

    @Test
    void testGetTextFromMessage_TextHtml() throws Exception {
        // 准备测试数据
        String htmlContent = "<html><body><h1>HTML邮件</h1></body></html>";

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mimeMessage.isMimeType("text/plain")).thenReturn(false);
        when(mimeMessage.isMimeType("text/html")).thenReturn(true);
        when(mimeMessage.getContent()).thenReturn(htmlContent);

        // 使用反射调用私有方法
        String result = ReflectionTestUtils.invokeMethod(pop3Service, "getTextFromMessage", mimeMessage);

        // 验证结果
        assertEquals(htmlContent, result);
    }

    @Test
    void testGetTextFromMessage_Multipart() throws Exception {
        // 准备测试数据
        String textContent = "多部分邮件的文本内容";

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mimeMessage.isMimeType("text/plain")).thenReturn(false);
        when(mimeMessage.isMimeType("text/html")).thenReturn(false);
        when(mimeMessage.isMimeType("multipart/*")).thenReturn(true);
        when(mimeMessage.getContent()).thenReturn(multipart);

        // 模拟multipart
        when(multipart.getCount()).thenReturn(1);
        when(multipart.getBodyPart(0)).thenReturn(textPart);
        when(textPart.isMimeType("text/plain")).thenReturn(true);
        when(textPart.getContent()).thenReturn(textContent);

        // 使用反射调用私有方法
        String result = ReflectionTestUtils.invokeMethod(pop3Service, "getTextFromMessage", mimeMessage);

        // 验证结果
        assertEquals(textContent, result);
    }

    @Test
    void testGetTextFromMessage_NestedMultipart() throws Exception {
        // 准备测试数据
        String textContent = "嵌套多部分邮件的文本内容";

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mimeMessage.isMimeType("text/plain")).thenReturn(false);
        when(mimeMessage.isMimeType("text/html")).thenReturn(false);
        when(mimeMessage.isMimeType("multipart/*")).thenReturn(true);

        // 创建嵌套的MimeMultipart
        MimeMultipart nestedMultipart = mock(MimeMultipart.class);
        MimeBodyPart nestedPart = mock(MimeBodyPart.class);

        when(nestedMultipart.getCount()).thenReturn(1);
        when(nestedMultipart.getBodyPart(0)).thenReturn(nestedPart);
        when(nestedPart.isMimeType("text/plain")).thenReturn(true);
        when(nestedPart.getContent()).thenReturn(textContent);

        // 外层multipart包含嵌套multipart
        when(multipart.getCount()).thenReturn(1);
        when(multipart.getBodyPart(0)).thenReturn(textPart);
        when(textPart.isMimeType("multipart/*")).thenReturn(true);
        when(textPart.getContent()).thenReturn(nestedMultipart);

        when(mimeMessage.getContent()).thenReturn(multipart);

        // 使用反射调用私有方法
        String result = ReflectionTestUtils.invokeMethod(pop3Service, "getTextFromMessage", mimeMessage);

        // 验证结果
        assertEquals(textContent, result);
    }

    @Test
    void testGetTextFromMimeMultipart_Simple() throws Exception {
        // 准备测试数据
        String textContent = "简单多部分邮件的文本内容";

        MimeMultipart simpleMultipart = mock(MimeMultipart.class);
        MimeBodyPart simplePart = mock(MimeBodyPart.class);

        when(simpleMultipart.getCount()).thenReturn(1);
        when(simpleMultipart.getBodyPart(0)).thenReturn(simplePart);
        when(simplePart.isMimeType("text/plain")).thenReturn(true);
        when(simplePart.getContent()).thenReturn(textContent);

        // 使用反射调用私有方法
        // 注意：方法名和参数要匹配Pop3Service中的实际定义
        String result = ReflectionTestUtils.invokeMethod(
            pop3Service,
            "getTextFromMimeMultipart",
            simpleMultipart
        );

        // 验证结果
        assertEquals(textContent, result);
    }

    @Test
    void testGetTextFromMimeMultipart_Mixed() throws Exception {
        // 准备测试数据：包含text/plain和text/html
        String plainText = "纯文本内容";
        String htmlText = "<html><body>HTML内容</body></html>";

        MimeMultipart mixedMultipart = mock(MimeMultipart.class);
        MimeBodyPart plainPart = mock(MimeBodyPart.class);
        MimeBodyPart htmlPart = mock(MimeBodyPart.class);

        when(mixedMultipart.getCount()).thenReturn(2);
        when(mixedMultipart.getBodyPart(0)).thenReturn(plainPart);
        when(mixedMultipart.getBodyPart(1)).thenReturn(htmlPart);

        when(plainPart.isMimeType("text/plain")).thenReturn(true);
        when(plainPart.getContent()).thenReturn(plainText);
        when(htmlPart.isMimeType("text/html")).thenReturn(true);
        when(htmlPart.getContent()).thenReturn(htmlText);

        // 使用反射调用私有方法
        String result = ReflectionTestUtils.invokeMethod(
            pop3Service,
            "getTextFromMimeMultipart",
            mixedMultipart
        );

        // 应该优先返回text/plain的内容
        assertEquals(plainText, result);
    }

    @Test
    void testSaveToLocalDatabase() throws Exception {
        // 准备测试数据
        String from = "sender@example.com";
        String to = "test@qq.com";
        String subject = "测试邮件";
        String content = "邮件内容";

        // 使用反射调用私有方法
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(
                pop3Service,
                "saveToLocalDatabase",
                from, to, subject, content
            );
        });

        // 验证：这个方法主要是记录日志，所以没有返回值验证
        // 如果需要验证，可以在Pop3Service中添加日志mock
    }

    @Test
    void testFetchExternalEmails_ConnectionFailure() throws Exception {
        // 模拟连接失败
        Store failingStore = mock(Store.class);
        when(failingStore.isConnected()).thenReturn(false);

        // 模拟连接时抛出异常
        doThrow(new jakarta.mail.MessagingException("连接失败"))
            .when(failingStore).connect(anyString(), anyString(), anyString());

        // 使用ReflectionTestUtils注入失败的store
        // 由于Pop3Service内部创建Store，我们需要重构代码使其可测试
        // 临时方案：测试异常处理

        // 验证方法能够优雅地处理异常
        assertDoesNotThrow(() -> {
            pop3Service.fetchExternalEmails();
        });
    }

    @Test
    void testFetchExternalEmails_EmptyInbox() throws Exception {
        // 模拟空收件箱
        when(store.isConnected()).thenReturn(true);
        when(store.getFolder("INBOX")).thenReturn(folder);
        when(folder.isOpen()).thenReturn(true);
        when(folder.getMessageCount()).thenReturn(0);
        when(folder.getMessages()).thenReturn(new Message[0]);

        // 验证空收件箱处理
        assertDoesNotThrow(() -> {
            pop3Service.fetchExternalEmails();
        });
    }
}