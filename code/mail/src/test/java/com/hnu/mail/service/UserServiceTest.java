package com.hnu.mail.service;

import com.hnu.mail.dto.ChangePasswordRequest;
import com.hnu.mail.dto.RegisterRequest;
import com.hnu.mail.dto.UpdateProfileRequest;
import com.hnu.mail.dto.UserProfile;
import com.hnu.mail.model.User;
import com.hnu.mail.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister_Success() {
        // 准备测试数据
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setNickname("测试用户");
        request.setPhone("13800138000");

        // 模拟依赖行为
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testuser");
        savedUser.setEmail("test@example.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setNickname("测试用户");
        savedUser.setPhone("13800138000");
        savedUser.setRole(User.UserRole.USER);
        savedUser.setStatus(User.UserStatus.ACTIVE);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // 执行测试
        User result = userService.register(request);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals(User.UserRole.USER, result.getRole());
        assertEquals(User.UserStatus.ACTIVE, result.getStatus());

        // 验证依赖方法调用
        verify(userRepository, times(1)).existsByUsername("testuser");
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegister_UsernameExists() {
        // 准备测试数据
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setEmail("new@example.com");
        request.setPassword("password123");

        // 模拟依赖行为
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.register(request);
        });

        assertEquals("用户名已存在", exception.getMessage());

        // 验证依赖方法调用
        verify(userRepository, times(1)).existsByUsername("existinguser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_EmailExists() {
        // 准备测试数据
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("existing@example.com");
        request.setPassword("password123");

        // 模拟依赖行为
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.register(request);
        });

        assertEquals("邮箱已被注册", exception.getMessage());

        // 验证依赖方法调用
        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, times(1)).existsByEmail("existing@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetProfile_Success() {
        // 准备测试数据
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setNickname("测试用户");
        user.setPhone("13800138000");
        user.setRole(User.UserRole.USER);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());

        // 模拟依赖行为
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // 执行测试
        UserProfile result = userService.getProfile(userId);

        // 验证结果
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("测试用户", result.getNickname());
        assertEquals("13800138000", result.getPhone());

        // 验证依赖方法调用
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testGetProfile_UserNotFound() {
        // 准备测试数据
        Long userId = 999L;

        // 模拟依赖行为
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.getProfile(userId);
        });

        assertEquals("用户不存在", exception.getMessage());

        // 验证依赖方法调用
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testUpdateProfile_Success() {
        // 准备测试数据
        Long userId = 1L;
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname("更新后的昵称");
        request.setPhone("13900139000");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("testuser");
        existingUser.setEmail("test@example.com");
        existingUser.setNickname("旧昵称");
        existingUser.setPhone("13800138000");

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setUsername("testuser");
        updatedUser.setEmail("test@example.com");
        updatedUser.setNickname("更新后的昵称");
        updatedUser.setPhone("13900139000");
        updatedUser.setUpdatedAt(LocalDateTime.now());

        // 模拟依赖行为
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // 执行测试
        UserProfile result = userService.updateProfile(userId, request);

        // 验证结果
        assertNotNull(result);
        assertEquals("更新后的昵称", result.getNickname());
        assertEquals("13900139000", result.getPhone());

        // 验证依赖方法调用
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testChangePassword_Success() {
        // 准备测试数据
        Long userId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldpassword");
        request.setNewPassword("newpassword");

        User user = new User();
        user.setId(userId);
        user.setPassword("encodedOldPassword");

        // 模拟依赖行为
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldpassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedNewPassword");

        // 执行测试
        assertDoesNotThrow(() -> userService.changePassword(userId, request));

        // 验证依赖方法调用
        verify(userRepository, times(1)).findById(userId);
        verify(passwordEncoder, times(1)).matches("oldpassword", "encodedOldPassword");
        verify(passwordEncoder, times(1)).encode("newpassword");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testChangePassword_OldPasswordIncorrect() {
        // 准备测试数据
        Long userId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongpassword");
        request.setNewPassword("newpassword");

        User user = new User();
        user.setId(userId);
        user.setPassword("encodedOldPassword");

        // 模拟依赖行为
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encodedOldPassword")).thenReturn(false);

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.changePassword(userId, request);
        });

        assertEquals("原密码错误", exception.getMessage());

        // 验证依赖方法调用
        verify(userRepository, times(1)).findById(userId);
        verify(passwordEncoder, times(1)).matches("wrongpassword", "encodedOldPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}
