package com.hnu.mail;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class PasswordHashTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testPasswordHash() {
        // 测试admin用户
        String adminPassword = "admin123";
        String adminHashFromDatabase = "$2a$10$5x7piehSC7ZcenbuV2pZJ.ifT1BxuqqPTHkAOD9ULCC/3rg9p2IX2";
        
        System.out.println("=== 管理员账户测试 ===");
        System.out.println("测试密码: " + adminPassword);
        System.out.println("数据库哈希值: " + adminHashFromDatabase);
        System.out.println("匹配结果: " + passwordEncoder.matches(adminPassword, adminHashFromDatabase));
        
        // 生成新的哈希值用于比较
        String adminNewHash = passwordEncoder.encode(adminPassword);
        System.out.println("新生成的哈希值: " + adminNewHash);
        System.out.println("新哈希值与密码匹配: " + passwordEncoder.matches(adminPassword, adminNewHash));
        
        // 测试user1用户
        String user1Password = "test123";
        
        System.out.println("\n=== 普通用户账户测试 ===");
        System.out.println("测试密码: " + user1Password);
        String user1NewHash = passwordEncoder.encode(user1Password);
        System.out.println("新生成的哈希值: " + user1NewHash);
        System.out.println("新哈希值与密码匹配: " + passwordEncoder.matches(user1Password, user1NewHash));
    }
}