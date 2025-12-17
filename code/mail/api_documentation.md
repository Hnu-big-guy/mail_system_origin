# 邮件系统后端API接口文档

## 1. 认证相关接口

### 1.1 用户登录
- **接口路径**: `/api/auth/login`
- **请求方法**: POST
- **功能描述**: 用户登录，获取JWT令牌
- **请求体**: 
  ```json
  {
    "username": "用户名",
    "password": "密码"
  }
  ```
- **响应**: 
  ```json
  {
    "token": "JWT令牌",
    "id": "用户ID",
    "username": "用户名",
    "email": "邮箱地址",
    "role": "用户角色"
  }
  ```
- **认证要求**: 无（公开接口）

### 1.2 用户注册
- **接口路径**: `/api/auth/register`
- **请求方法**: POST
- **功能描述**: 新用户注册
- **请求体**: 
  ```json
  {
    "username": "用户名",
    "email": "邮箱地址",
    "password": "密码"
  }
  ```
- **响应**: 
  ```json
  {
    "success": true,
    "message": "注册成功"
  }
  ```
- **认证要求**: 无（公开接口）

### 1.3 用户登出
- **接口路径**: `/api/auth/logout`
- **请求方法**: POST
- **功能描述**: 用户登出，清除会话
- **请求体**: 无
- **响应**: 
  ```json
  {
    "success": true,
    "message": "登出成功"
  }
  ```
- **认证要求**: 需要JWT令牌

### 1.4 获取当前用户信息
- **接口路径**: `/api/auth/profile`
- **请求方法**: GET
- **功能描述**: 获取当前登录用户的详细信息
- **请求体**: 无
- **响应**: 用户信息对象
- **认证要求**: 需要JWT令牌

## 2. 邮件相关接口

### 2.1 发送邮件
- **接口路径**: `/api/mail/send`
- **请求方法**: POST
- **功能描述**: 发送新邮件，支持附件
- **请求体**: multipart/form-data格式
  - `request`: JSON对象，包含邮件基本信息
    ```json
    {
      "to": "收件人邮箱",
      "subject": "邮件主题",
      "content": "邮件内容",
      "isDraft": false
    }
    ```
  - `attachments`: 文件列表（可选）
- **响应**: 
  ```json
  {
    "success": true,
    "message": "邮件发送成功"
  }
  ```
- **认证要求**: 需要JWT令牌

### 2.2 获取收件箱邮件
- **接口路径**: `/api/mail/inbox`
- **请求方法**: GET
- **功能描述**: 获取当前用户的收件箱邮件（分页）
- **请求参数**: 
  - `page`: 页码（可选，默认1）
  - `size`: 每页条数（可选，默认20）
- **响应**: 分页的邮件列表
- **认证要求**: 需要JWT令牌

### 2.3 获取已发送邮件
- **接口路径**: `/api/mail/sent`
- **请求方法**: GET
- **功能描述**: 获取当前用户的已发送邮件（分页）
- **请求参数**: 
  - `page`: 页码（可选，默认1）
  - `size`: 每页条数（可选，默认20）
- **响应**: 分页的邮件列表
- **认证要求**: 需要JWT令牌

### 2.4 获取草稿邮件
- **接口路径**: `/api/mail/draft`
- **请求方法**: GET
- **功能描述**: 获取当前用户的草稿邮件（分页）
- **请求参数**: 
  - `page`: 页码（可选，默认1）
  - `size`: 每页条数（可选，默认20）
- **响应**: 分页的邮件列表
- **认证要求**: 需要JWT令牌

### 2.5 获取单封邮件详情
- **接口路径**: `/api/mail/{id}`
- **请求方法**: GET
- **功能描述**: 根据邮件ID获取邮件详情
- **请求参数**: 
  - `id`: 邮件ID（路径参数）
- **响应**: 邮件详细信息
- **认证要求**: 需要JWT令牌

### 2.6 删除邮件
- **接口路径**: `/api/mail/{id}`
- **请求方法**: DELETE
- **功能描述**: 根据邮件ID删除邮件
- **请求参数**: 
  - `id`: 邮件ID（路径参数）
- **响应**: 
  ```json
  {
    "success": true,
    "message": "邮件已删除"
  }
  ```
- **认证要求**: 需要JWT令牌

### 2.7 标记邮件为已读
- **接口路径**: `/api/mail/{id}/read`
- **请求方法**: PUT
- **功能描述**: 将指定邮件标记为已读
- **请求参数**: 
  - `id`: 邮件ID（路径参数）
- **响应**: 邮件详细信息
- **认证要求**: 需要JWT令牌

### 2.8 移动邮件到指定文件夹
- **接口路径**: `/api/mail/{id}/move`
- **请求方法**: PUT
- **功能描述**: 将邮件移动到指定文件夹
- **请求参数**: 
  - `id`: 邮件ID（路径参数）
  - `folder`: 目标文件夹名称（如INBOX, SENT, TRASH等）
- **响应**: 
  ```json
  {
    "success": true,
    "message": "邮件已移动"
  }
  ```
- **认证要求**: 需要JWT令牌

## 3. 管理员相关接口

### 3.1 用户管理
- **获取所有用户**: GET `/api/admin/users`（分页）
- **创建新用户**: POST `/api/admin/users`
- **更新用户状态**: PUT `/api/admin/users/{id}/status`
- **更新用户角色**: PUT `/api/admin/users/{id}/role`
- **删除用户**: DELETE `/api/admin/users/{id}`
- **重置用户密码**: PUT `/api/admin/users/{id}/reset-password`
- **更新用户邮箱大小**: PUT `/api/admin/users/{id}/mailbox-size`

### 3.2 邮件管理
- **群发邮件**: POST `/api/admin/mail/broadcast`

### 3.3 系统管理
- **获取系统统计信息**: GET `/api/admin/statistics`
- **获取系统日志**: GET `/api/admin/logs`（支持过滤）
- **管理过滤规则**: 增删改查 `/api/admin/filter-rules`
- **管理系统参数**: 增删改查 `/api/admin/configs`

### 3.4 服务管理
- **启动SMTP服务**: POST `/api/admin/services/smtp/start`
- **停止SMTP服务**: POST `/api/admin/services/smtp/stop`
- **重启SMTP服务**: POST `/api/admin/services/smtp/restart`
- **启动POP3服务**: POST `/api/admin/services/pop3/start`
- **停止POP3服务**: POST `/api/admin/services/pop3/stop`
- **重启POP3服务**: POST `/api/admin/services/pop3/restart`

## 4. 认证与授权说明

### 4.1 JWT令牌使用
- 所有需要认证的接口都需要在请求头中添加JWT令牌：
  ```
  Authorization: Bearer [JWT令牌]
  ```

### 4.2 角色权限
- **普通用户**: 可以访问认证接口和邮件相关接口
- **管理员**: 可以访问所有接口，包括管理员专用接口

## 5. Android移动端开发注意事项

### 5.1 主要使用的接口
在Android移动端开发中，主要会用到以下接口：
- 认证接口（登录、注册、获取用户信息）
- 邮件相关接口（发送邮件、查看收件箱/已发送/草稿、管理邮件）

### 5.2 数据格式
- 请求和响应主要使用JSON格式
- 发送邮件时使用multipart/form-data格式（支持附件）

### 5.3 错误处理
- 接口返回的错误信息通常包含在`message`字段中
- HTTP状态码：200表示成功，400表示请求错误，401表示未认证，403表示无权限，500表示服务器错误

### 5.4 文件上传
- 发送邮件时支持附件上传，需要使用multipart/form-data格式
- 注意文件大小限制（由服务器配置决定）

### 5.5 分页处理
- 邮件列表接口支持分页，需要处理分页参数和返回的分页信息

---

此文档列出了邮件系统后端提供的所有API接口，Android移动端开发时可以根据需要调用相应的接口来实现邮件系统的功能。



# Android端需要调用的后端接口文档
以下是Android端（包括普通用户和管理员）需要调用的所有后端接口及其数据格式：

## 一、认证相关接口
### 1. 用户登录
- 请求方法 ：POST
- 请求路径 ： /api/auth/login
- 请求参数 ：
  ```
  {
    "username": "string",    // 用户名
    "password": "string"     // 密码
  }
  ```
- 响应格式 ：
  ```
  {
    "token": "string",       // JWT令牌
    "type": "Bearer",        // 令牌类型
    "id": 123,               // 用户ID
    "username": "string",    // 用户名
    "email": "string",       // 邮箱
    "role": "ROLE_USER"      // 用户角色（ROLE_USER 或 ROLE_ADMIN）
  }
  ```
- 功能说明 ：用户登录，获取JWT令牌用于后续接口调用
### 2. 用户注册
- 请求方法 ：POST
- 请求路径 ： /api/auth/register
- 请求参数 ：
  ```
  {
    "username": "string",    // 用户名
    "email": "string",       // 邮箱
    "password": "string",    // 密码
    "nickname": "string",    // 昵称（可选）
    "phone": "string"        // 手机号（可选）
  }
  ```
- 响应格式 ：
  ```
  {
    "success": true,         // 注册是否成功
    "message": "注册成功"     // 提示信息
  }
  ```
- 功能说明 ：普通用户注册
### 3. 用户登出
- 请求方法 ：POST
- 请求路径 ： /api/auth/logout
- 请求参数 ：无（需携带JWT令牌）
- 响应格式 ：
  ```
  {
    "success": true,         // 登出是否成功
    "message": "登出成功"     // 提示信息
  }
  ```
- 功能说明 ：用户登出
### 4. 获取个人信息
- 请求方法 ：GET
- 请求路径 ： /api/auth/profile
- 请求参数 ：无（需携带JWT令牌）
- 响应格式 ：
  ```
  {
    "id": 123,               // 用户ID
    "username": "string",    // 用户名
    "email": "string",       // 邮箱
    "nickname": "string",    // 昵称
    "phone": "string",       // 手机号
    "role": "ROLE_USER",     // 用户角色
    "status": "ACTIVE",      // 用户状态
    "mailboxSize": 500,      // 邮箱大小（MB）
    "usedSize": 100,         // 已用空间（MB）
    "lastLoginTime": "2023-10-01T10:00:00",  // 最后登录时间
    "createdAt": "2023-09-01T10:00:00"        // 创建时间
  }
  ```
- 功能说明 ：获取当前登录用户的个人信息
## 二、普通用户邮件操作接口
### 1. 发送邮件
- 请求方法 ：POST
- 请求路径 ： /api/mail/send
- 请求参数 ：
  - Form Data：
    - request ：JSON格式的邮件内容
    - attachments ：附件列表（可选）
  ```
  {
    "to": "string",          // 收件人邮箱
    "subject": "string",     // 邮件主题
    "content": "string",     // 邮件内容
    "from": "string"         // 发件人邮箱（可选，系统自动填充）
  }
  ```
- 响应格式 ：
  ```
  {
    "success": true,         // 发送是否成功
    "message": "邮件发送成功"  // 提示信息
  }
  ```
- 功能说明 ：发送邮件，支持附件
### 2. 获取收件箱
- 请求方法 ：GET
- 请求路径 ： /api/mail/inbox?page=0&size=20
- 请求参数 ：
  - page ：页码（可选，默认0）
  - size ：每页数量（可选，默认20）
- 响应格式 ：
  ```
  {
    "content": [
      {
        "id": 123,           // 邮件ID
        "subject": "string", // 邮件主题
        "content": "string", // 邮件内容
        "from": "string",    // 发件人
        "to": "string",      // 收件人
        "sentAt": "2023-10-01T10:00:00",  // 发送时间
        "isRead": false,     // 是否已读
        "isStarred": false,  // 是否星标
        "folder": "INBOX",   // 所属文件夹
        "size": 1024,        // 邮件大小（字节）
        "attachments": []    // 附件列表
      }
    ],
    "totalPages": 5,         // 总页数
    "totalElements": 100,    // 总邮件数
    "last": false,           // 是否最后一页
    "first": true,           // 是否第一页
    "size": 20,              // 每页数量
    "number": 0,             // 当前页码
    "sort": [],              // 排序信息
    "numberOfElements": 20   // 当前页邮件数
  }
  ```
- 功能说明 ：获取收件箱邮件，支持分页
### 3. 获取已发送邮件
- 请求方法 ：GET
- 请求路径 ： /api/mail/sent?page=0&size=20
- 请求参数 ：与获取收件箱相同
- 响应格式 ：与获取收件箱相同
- 功能说明 ：获取已发送邮件，支持分页
### 4. 获取草稿箱邮件
- 请求方法 ：GET
- 请求路径 ： /api/mail/draft?page=0&size=20
- 请求参数 ：与获取收件箱相同
- 响应格式 ：与获取收件箱相同
- 功能说明 ：获取草稿箱邮件，支持分页
### 5. 获取单封邮件详情
- 请求方法 ：GET
- 请求路径 ： /api/mail/{id}
- 请求参数 ： id （邮件ID）
- 响应格式 ：
  ```
  {
    "id": 123,               // 邮件ID
    "subject": "string",     // 邮件主题
    "content": "string",     // 邮件内容
    "from": "string",        // 发件人
    "to": "string",          // 收件人
    "sentAt": "2023-10-01T10:00:00",  // 发送时间
    "isRead": true,          // 是否已读
    "isStarred": false,      // 是否星标
    "folder": "INBOX",       // 所属文件夹
    "size": 1024,            // 邮件大小（字节）
    "attachments": []        // 附件列表
  }
  ```
- 功能说明 ：获取单封邮件的详细信息
### 6. 删除邮件
- 请求方法 ：DELETE
- 请求路径 ： /api/mail/{id}
- 请求参数 ： id （邮件ID）
- 响应格式 ：
  ```
  {
    "success": true,         // 删除是否成功
    "message": "邮件已删除"   // 提示信息
  }
  ```
- 功能说明 ：删除指定邮件
### 7. 标记邮件为已读
- 请求方法 ：PUT
- 请求路径 ： /api/mail/{id}/read
- 请求参数 ： id （邮件ID）
- 响应格式 ：与获取单封邮件详情相同
- 功能说明 ：将指定邮件标记为已读
### 8. 移动邮件到其他文件夹
- 请求方法 ：PUT
- 请求路径 ： /api/mail/{id}/move?folder=TRASH
- 请求参数 ：
  - id （邮件ID）
  - folder （目标文件夹，可选值：INBOX、SENT、DRAFT、TRASH、SPAM）
- 响应格式 ：
  ```
  {
    "success": true,         // 移动是否成功
    "message": "邮件已移动"   // 提示信息
  }
  ```
- 功能说明 ：将邮件移动到指定文件夹
## 三、管理员专用接口
### 1. 获取所有用户
- 请求方法 ：GET
- 请求路径 ： /api/admin/users?page=0&size=20
- 请求参数 ：
  - page ：页码（可选，默认0）
  - size ：每页数量（可选，默认20）
- 响应格式 ：分页用户列表
- 功能说明 ：获取所有用户信息
### 2. 创建用户
- 请求方法 ：POST
- 请求路径 ： /api/admin/users
- 请求参数 ：
  ```
  {
    "username": "string",    // 用户名
    "email": "string",       // 邮箱
    "password": "string",    // 密码
    "nickname": "string",    // 昵称（可选）
    "phone": "string",       // 手机号（可选）
    "role": "ROLE_USER",     // 用户角色（ROLE_USER 或 ROLE_ADMIN）
    "status": "ACTIVE"       // 用户状态（ACTIVE 或 DISABLED）
  }
  ```
- 响应格式 ：
  ```
  {
    "success": true,         // 创建是否成功
    "message": "用户创建成功"  // 提示信息
  }
  ```
- 功能说明 ：管理员创建新用户
### 3. 更新用户状态
- 请求方法 ：PUT
- 请求路径 ： /api/admin/users/{id}/status?status=DISABLED
- 请求参数 ：
  - id ：用户ID
  - status ：用户状态（ACTIVE 或 DISABLED）
- 响应格式 ：
  ```
  {
    "success": true,         // 更新是否成功
    "message": "用户状态已更新" // 提示信息
  }
  ```
- 功能说明 ：启用或禁用用户
### 4. 删除用户
- 请求方法 ：DELETE
- 请求路径 ： /api/admin/users/{id}
- 请求参数 ： id （用户ID）
- 响应格式 ：
  ```
  {
    "success": true,         // 删除是否成功
    "message": "用户已删除"   // 提示信息
  }
  ```
- 功能说明 ：删除指定用户
### 5. 群发邮件
- 请求方法 ：POST
- 请求路径 ： /api/admin/mail/broadcast
- 请求参数 ：
  ```
  {
    "subject": "string",     // 邮件主题
    "content": "string",     // 邮件内容
    "recipientGroups": [],   // 接收用户组（可选）
    "recipientIds": [1, 2, 3] // 特定用户ID列表（可选）
  }
  ```
- 响应格式 ：
  ```
  {
    "success": true,         // 群发是否成功
    "message": "群发邮件任务已启动" // 提示信息
  }
  ```
- 功能说明 ：向指定用户或用户组发送群发邮件
### 6. 获取系统统计信息
- 请求方法 ：GET
- 请求路径 ： /api/admin/statistics
- 请求参数 ：无
- 响应格式 ：
  ```
  {
    "totalUsers": 100,       // 总用户数
    "totalMails": 1000,      // 总邮件数
    "todayMails": 50,        // 今日邮件数
    "onlineUsers": 10        // 在线用户数
  }
  ```
- 功能说明 ：获取系统统计数据
### 7. SMTP服务管理
- 启动SMTP服务 ：POST /api/admin/services/smtp/start
- 停止SMTP服务 ：POST /api/admin/services/smtp/stop
- 重启SMTP服务 ：POST /api/admin/services/smtp/restart
### 8. POP3服务管理
- 启动POP3服务 ：POST /api/admin/services/pop3/start
- 停止POP3服务 ：POST /api/admin/services/pop3/stop
- 重启POP3服务 ：POST /api/admin/services/pop3/restart
### 9. 日志管理
- 获取SMTP日志 ：GET /api/admin/logs/smtp?page=0&size=50
- 获取POP3日志 ：GET /api/admin/logs/pop3?page=0&size=50
- 删除旧日志 ：DELETE /api/admin/logs/old?beforeDate=2023-09-01T00:00:00
- 清空所有日志 ：DELETE /api/admin/logs/all
## 四、通用说明
1. 认证要求 ：所有接口（除了登录和注册）都需要在请求头中添加认证信息：
   
   ```
   Authorization: Bearer {token}
   ```
   其中 {token} 是登录接口返回的JWT令牌。
2. 分页参数 ：所有分页接口都支持以下参数：
   
   - page ：页码（从0开始）
   - size ：每页数量
   - sort ：排序字段（如 sentAt,desc 表示按发送时间降序）
3. 错误响应 ：当请求失败时，响应格式为：
   
   ```
   {
     "success": false,        // 操作是否成功
     "message": "错误信息"     // 错误详情
   }
   ```
4. 文件上传 ：发送邮件时，附件需要使用 multipart/form-data 格式上传。
以上是Android端需要调用的所有后端接口，根据用户角色（普通用户或管理员）调用相应的接口即可实现所需功能。