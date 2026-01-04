# 邮件系统项目

这是一个完整的邮件系统项目，包含后端服务和Android客户端应用。

## 技术栈

### 后端服务
- **语言**: Java
- **框架**: Spring Boot
- **构建工具**: Maven
- **主要功能**:
  - SMTP/POP3邮件服务器
  - 用户认证与授权（JWT）
  - 邮件发送、接收与管理
  - 邮件过滤规则
  - 管理员功能
  - WebSocket实时通知

### Android客户端
- **语言**: Kotlin
- **框架**: Android Jetpack
- **构建工具**: Gradle
- **主要功能**:
  - 邮件列表展示
  - 邮件详情查看
  - 邮件发送功能
  - 用户认证
  - 消息推送

## 项目结构

```
code/
├── android/              # Android客户端
│   ├── app/              # 应用代码
│   └── gradle/           # Gradle配置
└── mail/                 # 后端服务
    ├── src/              # 源代码
    │   ├── main/java/    # Java代码
    │   └── resources/    # 资源文件
    └── pom.xml           # Maven配置
```

## 快速开始

### 后端服务

1. 确保已安装 JDK 11+ 和 Maven
2. 进入 `code/mail` 目录
3. 配置数据库连接（在 `application.properties` 中）
4. 运行服务：
   ```bash
   ./mvnw spring-boot:run
   ```

### Android客户端

1. 确保已安装 Android Studio 和 Android SDK
2. 打开 `code/android` 目录
3. 配置后端服务地址（在 `local.properties` 中）
4. 构建并运行应用

## 主要功能

### 后端功能
- 用户注册与登录
- 邮件发送（SMTP）
- 邮件接收（POP3）
- 邮件过滤与分类
- 管理员用户管理
- 系统日志记录
- 统计信息查看

### 客户端功能
- 邮件列表展示
- 邮件详情阅读
- 邮件撰写与发送
- 附件管理
- 邮件搜索
- 主题切换

## 配置文件

### 后端配置
- `application.properties` - 主配置文件
- `mail.properties` - 邮件服务器配置
- `jwt.properties` - JWT认证配置

### Android配置
- `local.properties` - 本地配置（服务器地址等）

## 注意事项

- 首次运行需要初始化数据库，请执行 `resources/mail_system.sql` 脚本
- 确保端口 25（SMTP）和 110（POP3）未被占用
- 生产环境请修改默认密码和密钥


