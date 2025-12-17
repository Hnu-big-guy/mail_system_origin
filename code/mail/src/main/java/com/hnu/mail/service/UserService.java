// UserService.java
package com.hnu.mail.service;

import com.hnu.mail.dto.*;
import com.hnu.mail.model.User;
import com.hnu.mail.repository.UserRepository;
import com.hnu.mail.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public User register(RegisterRequest request) {
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
    user.setRole(User.UserRole.USER);
    user.setStatus(User.UserStatus.ACTIVE);

    return userRepository.save(user);
  }

  @Transactional(readOnly = true)
  public UserProfile getProfile(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("用户不存在"));

    return mapToProfile(user);
  }

  @Transactional
  public UserProfile updateProfile(Long userId, UpdateProfileRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("用户不存在"));

    user.setNickname(request.getNickname());
    user.setPhone(request.getPhone());
    user.setUpdatedAt(LocalDateTime.now());

    userRepository.save(user);
    return mapToProfile(user);
  }

  @Transactional
  public void changePassword(Long userId, ChangePasswordRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("用户不存在"));

    if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
      throw new RuntimeException("原密码错误");
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    user.setUpdatedAt(LocalDateTime.now());
    userRepository.save(user);
  }

  private UserProfile mapToProfile(User user) {
    UserProfile profile = new UserProfile();
    profile.setId(user.getId());
    profile.setUsername(user.getUsername());
    profile.setEmail(user.getEmail());
    profile.setNickname(user.getNickname());
    profile.setPhone(user.getPhone());
    profile.setRole(user.getRole());
    profile.setStatus(user.getStatus());
    profile.setMailboxSize(user.getMailboxSize());
    profile.setUsedSize(user.getUsedSize());
    profile.setLastLoginTime(user.getLastLoginTime());
    profile.setCreatedAt(user.getCreatedAt());
    return profile;
  }
}