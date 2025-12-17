// User.java
package com.hnu.mail.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Size(max = 50)
  @Column(unique = true)
  private String username;

  @NotBlank
  @Size(max = 100)
  @Email
  @Column(unique = true)
  private String email;

  @NotBlank
  @Size(max = 120)
  private String password;

  @Size(max = 50)
  private String nickname;

  @Size(max = 20)
  private String phone;

  @Enumerated(EnumType.STRING)
  private UserRole role = UserRole.USER;

  @Enumerated(EnumType.STRING)
  private UserStatus status = UserStatus.ACTIVE;

  private Integer mailboxSize = 100; // 邮箱容量(MB)

  private Integer usedSize = 0; // 已使用大小(KB)

  private LocalDateTime lastLoginTime;

  private LocalDateTime createdAt = LocalDateTime.now();

  private LocalDateTime updatedAt = LocalDateTime.now();

  @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Mail> sentMails = new HashSet<>();

  @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Mail> receivedMails = new HashSet<>();

  public enum UserRole {
    USER, ADMIN
  }

  public enum UserStatus {
    ACTIVE, DISABLED, LOCKED
  }
}