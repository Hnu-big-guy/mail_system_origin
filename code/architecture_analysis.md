# 邮件系统逻辑架构分析

## 1. 系统分层架构

该邮件系统采用典型的Spring Boot分层架构，确保关注点分离和代码可维护性：

```
┌─────────────────────────────────────────────────────────────────┐
│                       表示层 (Presentation Layer)              │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐       │
│  │  AuthController │ │ MailController │ │AdminController│       │
│  └───────────────┘  └───────────────┘  └───────────────┘       │
├─────────────────────────────────────────────────────────────────┤
│                       业务逻辑层 (Service Layer)               │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐       │
│  │   UserService  │ │  MailService   │ │  AdminService  │       │
│  ├───────────────┤  ├───────────────┤  ├───────────────┤       │
│  │Pop3Service    │ │ FilterService   │ │UserDetailsServiceImpl ││
│  └───────────────┘  └───────────────┘  └───────────────┘       │
├─────────────────────────────────────────────────────────────────┤
│                       数据访问层 (Repository Layer)             │
│  ┌───────────────┐  ┌───────────────┐                          │
│  │UserRepository │ │ MailRepository │                          │
│  └───────────────┘  └───────────────┘                          │
├─────────────────────────────────────────────────────────────────┤
│                       数据模型层 (Model Layer)                  │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐       │
│  │     User      │ │      Mail      │ │   Attachment  │       │
│  └───────────────┘  └───────────────┘  └───────────────┘       │
└─────────────────────────────────────────────────────────────────┘
```

## 2. 核心功能信息流程图

### 2.1 用户注册登录流程

```
客户端 → AuthController → [UserService / AuthenticationManager] → UserRepository → 数据库
```

**详细步骤：**
1. 客户端发送注册/登录请求到`AuthController`
2. 注册请求：`UserService`验证用户名/邮箱唯一性 → 加密密码 → 保存用户信息
3. 登录请求：`AuthenticationManager`进行身份认证 → 生成JWT令牌 → 返回给客户端
4. JWT令牌包含用户身份信息，用于后续API请求认证

### 2.2 邮件发送流程

```
客户端 → MailController → MailService → [FilterService / JavaMailSender] → MailRepository → 数据库
```

**详细步骤：**
1. 客户端发送邮件请求到`MailController`（包含收件人、主题、内容、附件）
2. `MailService`验证发件人/收件人存在性 → 检查邮箱容量 → 应用邮件过滤规则
3. 创建邮件记录 → 保存到`MailRepository`
4. 异步调用`JavaMailSender`发送实际邮件（支持附件）
5. 更新用户已使用邮箱空间

### 2.3 邮件接收流程

```
POP3服务器 ← Pop3Service ← 定时任务 → 数据库
```

**详细步骤：**
1. `Pop3Service`通过`@Scheduled`注解每小时执行一次邮件拉取任务
2. 连接到外部POP3服务器 → 获取未读邮件
3. 解析邮件内容（支持text/plain、text/html、multipart/*格式）
4. 标记邮件为已读 → 保存到本地数据库（待实现完整逻辑）

### 2.4 管理员功能流程

```
管理员客户端 → AdminController → AdminService → [UserRepository / MailRepository] → 数据库
```

**详细步骤：**
1. 管理员登录后访问管理API
2. 用户管理：查询/创建/更新/删除用户信息
3. 群发邮件：获取所有用户 → 遍历发送邮件给每个用户
4. 系统统计：获取用户数量、邮件数量等统计信息

## 3. 关键组件职责

### 3.1 控制器层 (Controller)

| 控制器名称 | 主要职责 | 核心API |
|------------|----------|---------|
| `AuthController` | 用户认证 | `/api/auth/login`, `/api/auth/register` |
| `MailController` | 邮件操作 | `/api/mail/send`, `/api/mail/inbox`, `/api/mail/{id}` |
| `AdminController` | 管理功能 | `/api/admin/users`, `/api/admin/mail/broadcast` |

### 3.2 服务层 (Service)

| 服务名称 | 主要职责 | 核心方法 |
|----------|----------|---------|
| `UserService` | 用户管理 | `register()`, `updateProfile()`, `changePassword()` |
| `MailService` | 邮件处理 | `sendMail()`, `getUserMails()`, `deleteMail()` |
| `AdminService` | 管理功能 | `broadcastMail()`, `getStatistics()`, `updateUserStatus()` |
| `Pop3Service` | POP3邮件拉取 | `fetchExternalEmails()`, `getTextFromMessage()` |
| `FilterService` | 邮件过滤 | `applyFilters()` |

### 3.3 数据访问层 (Repository)

| 仓库名称 | 主要职责 | 核心方法 |
|----------|----------|---------|
| `UserRepository` | 用户数据访问 | `findByUsername()`, `countByStatus()` |
| `MailRepository` | 邮件数据访问 | `findByReceiverAndFolder()`, `countBySentAtAfter()` |

## 4. 数据流转示例

### 4.1 用户发送邮件示例

```
1. 客户端 POST /api/mail/send 请求
2. MailController 接收请求，认证用户
3. MailService.sendMail() 执行：
   a. 验证用户存在性
   b. 检查邮箱容量
   c. 应用邮件过滤
   d. 创建邮件记录
   e. 保存到数据库
   f. 异步调用JavaMailSender发送邮件
4. 返回发送结果给客户端
```

### 4.2 定时接收邮件示例

```
1. @Scheduled触发Pop3Service.fetchExternalEmails()
2. 连接POP3服务器
3. 获取未读邮件列表
4. 遍历处理每封邮件：
   a. 解析邮件内容
   b. 标记为已读
   c. 保存到本地数据库
5. 关闭连接，记录日志
```

## 5. 技术栈与依赖关系

| 技术/框架 | 用途 | 依赖关系 |
|-----------|------|---------|
| Spring Boot | 应用框架 | 所有模块的基础 |
| Spring Security | 安全认证 | AuthController, JwtTokenProvider |
| Spring Data JPA | 数据访问 | 所有Repository |
| JavaMail | 邮件处理 | MailService, Pop3Service |
| JWT | 身份验证 | AuthController, JwtTokenProvider |
| MySQL | 数据库存储 | 所有数据持久化 |

## 6. 系统扩展点

1. **邮件过滤系统**：当前为基础实现，可扩展更复杂的过滤规则
2. **存储策略**：支持本地存储和云存储切换
3. **邮件搜索功能**：可扩展全文搜索能力
4. **WebSockets**：已配置但未完全使用，可实现实时通知

## 7. 安全性设计

1. **JWT认证**：无状态认证，防止CSRF攻击
2. **密码加密**：使用BCrypt密码哈希
3. **角色控制**：基于RBAC的权限管理（USER/ADMIN）
4. **输入验证**：所有请求参数进行合法性验证
5. **CORS配置**：限制跨域请求来源

---

通过以上分析，您可以全面了解该邮件系统的整体架构、信息流程和核心功能实现。系统采用模块化设计，各组件职责清晰，便于维护和扩展。