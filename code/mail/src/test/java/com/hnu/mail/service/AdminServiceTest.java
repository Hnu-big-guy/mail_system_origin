package com.hnu.mail.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hnu.mail.dto.AdminCreateUserRequest;
import com.hnu.mail.dto.BroadcastMailRequest;
import com.hnu.mail.dto.StatisticsDto;
import com.hnu.mail.dto.SystemLogDto;
import com.hnu.mail.model.Mail;
import com.hnu.mail.model.SystemLog;
import com.hnu.mail.model.User;
import com.hnu.mail.repository.MailRepository;
import com.hnu.mail.repository.UserRepository;

class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MailRepository mailRepository;

    @Mock
    private MailService mailService;

    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private LogService logService;

    @InjectMocks
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllUsers() {
        // 准备测试数据
        Pageable pageable = Pageable.unpaged();

        // 模拟依赖行为
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");

        List<User> userList = List.of(user1, user2);
        Page<User> userPage = new PageImpl<>(userList);
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // 执行测试
        Page<User> result = adminService.getAllUsers(pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("user1", result.getContent().get(0).getUsername());
        assertEquals("user2", result.getContent().get(1).getUsername());

        // 验证依赖方法调用
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    void testCreateUser_Success() {
        // 准备测试数据
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setNickname("新用户");
        request.setPhone("13800138000");

        // 模拟依赖行为
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(3L);
        savedUser.setUsername("newuser");
        savedUser.setEmail("newuser@example.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setNickname("新用户");
        savedUser.setPhone("13800138000");
        savedUser.setRole(User.UserRole.USER);
        savedUser.setStatus(User.UserStatus.ACTIVE);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // 执行测试
        User result = adminService.createUser(request);

        // 验证结果
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("newuser", result.getUsername());
        assertEquals(User.UserRole.USER, result.getRole());
        assertEquals(User.UserStatus.ACTIVE, result.getStatus());

        // 验证依赖方法调用
        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, times(1)).existsByEmail("newuser@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUser_UsernameExists() {
        // 准备测试数据
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername("existinguser");
        request.setEmail("new@example.com");

        // 模拟依赖行为
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminService.createUser(request);
        });

        assertEquals("用户名已存在", exception.getMessage());
    }

    @Test
    void testUpdateUserStatus_Success() {
        // 准备测试数据
        Long userId = 1L;
        String status = "disabled";

        // 模拟依赖行为
        User user = new User();
        user.setId(userId);
        user.setStatus(User.UserStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // 执行测试
        assertDoesNotThrow(() -> adminService.updateUserStatus(userId, status));

        // 验证结果
        assertEquals(User.UserStatus.DISABLED, user.getStatus());

        // 验证依赖方法调用
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testUpdateUserStatus_UserNotFound() {
        // 准备测试数据
        Long userId = 999L;
        String status = "disabled";

        // 模拟依赖行为
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminService.updateUserStatus(userId, status);
        });

        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    void testUpdateUserRole_Success() {
        // 准备测试数据
        Long userId = 1L;
        String role = "admin";

        // 模拟依赖行为
        User user = new User();
        user.setId(userId);
        user.setRole(User.UserRole.USER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // 执行测试
        assertDoesNotThrow(() -> adminService.updateUserRole(userId, role));

        // 验证结果
        assertEquals(User.UserRole.ADMIN, user.getRole());

        // 验证依赖方法调用
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testDeleteUser_Success() {
        // 准备测试数据
        Long userId = 1L;

        // 模拟依赖行为
        doNothing().when(userRepository).deleteById(userId);

        // 执行测试
        assertDoesNotThrow(() -> adminService.deleteUser(userId));

        // 验证依赖方法调用
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void testBroadcastMail_Success() throws Exception {
        // 准备测试数据
        BroadcastMailRequest request = new BroadcastMailRequest();
        request.setSubject("群发测试邮件");
        request.setContent("这是一封群发测试邮件");
        Long adminId = 1L;

        // 模拟依赖行为
        User admin = new User();
        admin.setId(adminId);
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

        List<Long> userIds = List.of(2L, 3L);
        when(userRepository.findAllUserIds()).thenReturn(userIds);

        User user1 = new User();
        user1.setId(2L);
        user1.setEmail("user1@example.com");

        User user2 = new User();
        user2.setId(3L);
        user2.setEmail("user2@example.com");

        when(userRepository.findById(2L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(3L)).thenReturn(Optional.of(user2));

        when(mailService.sendMail(any(), eq(adminId), anyList())).thenReturn(new Mail());

        // 执行测试
        assertDoesNotThrow(() -> adminService.broadcastMail(request, adminId));

        // 验证依赖方法调用
        verify(userRepository, times(1)).findById(adminId);
        verify(userRepository, times(1)).findAllUserIds();
        verify(userRepository, times(1)).findById(2L);
        verify(userRepository, times(1)).findById(3L);
        verify(mailService, times(2)).sendMail(any(), eq(adminId), anyList());
    }

    @Test
    void testGetStatistics() {
        // 准备测试数据
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

        // 模拟依赖行为
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countByStatus(User.UserStatus.ACTIVE)).thenReturn(95L);
        when(mailRepository.count()).thenReturn(500L);
        when(mailRepository.countBySentAtAfter(today)).thenReturn(20L);

        // 执行测试
        StatisticsDto result = adminService.getStatistics();

        // 验证结果
        assertNotNull(result);
        assertEquals(100L, result.getTotalUsers());
        assertEquals(95L, result.getActiveUsers());
        assertEquals(500L, result.getTotalMails());
        assertEquals(20L, result.getTodayMails());
        assertEquals(0L, result.getTotalAttachments()); // 尚未实现
        assertEquals(0L, result.getTotalStorageUsed()); // 尚未实现

        // 验证依赖方法调用
        verify(userRepository, times(1)).count();
        verify(userRepository, times(1)).countByStatus(User.UserStatus.ACTIVE);
        verify(mailRepository, times(1)).count();
        verify(mailRepository, times(1)).countBySentAtAfter(today);
    }

    @Test
    void testGetSystemLogs() {
        // 准备测试数据
        String type = "ERROR";
        String startDate = "2023-01-01";
        String endDate = "2023-12-31";
        Pageable pageable = Pageable.unpaged();

        // 模拟依赖行为
        Page<SystemLog> emptyPage = Page.empty();
        when(logService.getLogsByType(any(SystemLog.LogType.class), any(Pageable.class))).thenReturn(emptyPage);

        // 执行测试
        Page<SystemLogDto> result = adminService.getSystemLogs(type, startDate, endDate, pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        
        // 验证依赖方法调用
        verify(logService, times(1)).getLogsByType(any(SystemLog.LogType.class), any(Pageable.class));
    }
}
