// AuthController.java
package com.hnu.mail.controller;

import com.hnu.mail.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hnu.mail.model.SystemLog;
import com.hnu.mail.model.User;
import com.hnu.mail.security.JwtTokenProvider;
import com.hnu.mail.security.UserPrincipal;
import com.hnu.mail.service.LogService;
import com.hnu.mail.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider tokenProvider;
  private final UserService userService;
  private final LogService logService;

  @PostMapping("/login")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getUsername(),
            request.getPassword()
        )
    );

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = tokenProvider.generateToken(authentication);

    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    // 更新最后登录时间
    updateLastLoginTime(userPrincipal.getId());

    // 记录登录日志
    String ipAddress = getClientIp(httpRequest);
    logService.createLog(
        SystemLog.LogType.LOGIN,
        "认证模块",
        "用户登录",
        "用户" + userPrincipal.getUsername() + "成功登录系统",
        ipAddress,
        userPrincipal.getUsername()
    );

    return ResponseEntity.ok(new JwtResponse(
        jwt,
        userPrincipal.getId(),
        userPrincipal.getUsername(),
        userPrincipal.getEmail(),
        userPrincipal.getRole()
    ));
  }

  @PostMapping("/register")
  public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
    User user = userService.register(request);

    // 注册成功后自动登录，返回JWT token
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getUsername(),
            request.getPassword()
        )
    );

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = tokenProvider.generateToken(authentication);

    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    return ResponseEntity.ok(new JwtResponse(
        jwt,
        userPrincipal.getId(),
        userPrincipal.getUsername(),
        userPrincipal.getEmail(),
        userPrincipal.getRole()
    ));
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logoutUser(HttpServletRequest httpRequest) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = "匿名用户";
    
    // 检查是否为认证用户
    if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
      UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
      username = userPrincipal.getUsername();
      
      // 记录登出日志
      String ipAddress = getClientIp(httpRequest);
      logService.createLog(
          SystemLog.LogType.LOGOUT,
          "认证模块",
          "用户登出",
          "用户" + username + "成功登出系统",
          ipAddress,
          username
      );
    }
    
    // 清除安全上下文
    SecurityContextHolder.clearContext();
    
    return ResponseEntity.ok(new ApiResponse(true, "登出成功"));
  }

  @GetMapping("/profile")
  public ResponseEntity<?> getCurrentUser() {
    UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();

    UserProfile profile = userService.getProfile(userPrincipal.getId());
    return ResponseEntity.ok(profile);
  }

  @PostMapping("/profile")
  public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {
    UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();

    UserProfile updatedProfile = userService.updateProfile(userPrincipal.getId(), request);
    return ResponseEntity.ok(new ApiResponse(true, "个人信息更新成功"));
  }
  
  @PostMapping("/change-password")
  public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
    UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();

    try {
      userService.changePassword(userPrincipal.getId(), request);
      return ResponseEntity.ok(new ApiResponse(true, "密码修改成功"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse(false, e.getMessage()));
    }
  }

  private void updateLastLoginTime(Long userId) {
    // TODO: 更新用户最后登录时间
  }
  
  /**
   * 获取客户端IP地址
   */
  private String getClientIp(HttpServletRequest request) {
    String xfHeader = request.getHeader("X-Forwarded-For");
    if (xfHeader != null && !xfHeader.isEmpty()) {
      return xfHeader.split(",")[0];
    }
    return request.getRemoteAddr();
  }
}