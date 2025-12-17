package com.hnu.mail.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hnu.mail.model.Mail;
import com.hnu.mail.model.SystemConfig;
import com.hnu.mail.repository.MailRepository;
import com.hnu.mail.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class Pop3Server {

    @Autowired
    private SystemConfigService configService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailRepository mailRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private boolean running = false;

    /**
     * 启动POP3服务器
     */
    public void start() {
        if (running) {
            log.info("POP3服务器已经在运行中");
            return;
        }

        try {
            // 从系统配置获取端口和启用状态
            int port = Integer.parseInt(configService.getConfigValue(SystemConfig.POP3_SERVER_PORT, "110"));
            boolean enabled = Boolean.parseBoolean(configService.getConfigValue(SystemConfig.POP3_SERVER_ENABLED, "true"));
            
            if (!enabled) {
                log.info("POP3服务器已被禁用");
                return;
            }
            
            serverSocket = new ServerSocket(port);
            executorService = Executors.newCachedThreadPool();
            running = true;

            log.info("POP3服务器已启动，监听端口: {}", port);

            // 启动线程监听客户端连接
            new Thread(() -> {
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        log.info("新的POP3客户端连接: {}", clientSocket.getInetAddress());
                        executorService.submit(new Pop3ClientHandler(clientSocket));
                    } catch (IOException e) {
                        if (running) {
                            log.error("POP3服务器接受连接失败: {}", e.getMessage());
                        }
                    }
                }
            }).start();

        } catch (IOException e) {
            log.error("启动POP3服务器失败: {}", e.getMessage());
            running = false;
        }
    }

    /**
     * 停止POP3服务器
     */
    public void stop() {
        if (!running) {
            log.info("POP3服务器已经停止");
            return;
        }

        running = false;

        try {
            if (serverSocket != null) {
                serverSocket.close();
            }

            if (executorService != null) {
                executorService.shutdownNow();
            }

            log.info("POP3服务器已停止");
        } catch (IOException e) {
            log.error("停止POP3服务器失败: {}", e.getMessage());
        }
    }

    /**
     * 重启POP3服务器
     */
    public void restart() {
        stop();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        start();
    }

    /**
     * 检查POP3服务器是否正在运行
     * @return 服务器运行状态
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * POP3客户端处理器
     */
    private class Pop3ClientHandler implements Runnable {

        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String currentUser;
        private Pop3State state;
        private Map<Integer, Mail> messageMap;
        private Map<Integer, Boolean> deletedMessages;

        public Pop3ClientHandler(Socket socket) {
            this.clientSocket = socket;
            this.state = Pop3State.AUTHORIZATION;
            this.messageMap = new HashMap<>();
            this.deletedMessages = new HashMap<>();
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // 发送欢迎消息
                sendResponse("+OK POP3 server ready");

                String line;
                while ((line = in.readLine()) != null) {
                    log.debug("收到POP3命令: {}", line);
                    handleCommand(line.trim());
                }
            } catch (IOException e) {
                log.error("处理POP3客户端连接时出错: {}", e.getMessage());
            } finally {
                closeConnection();
            }
        }

        /**
         * 处理POP3命令
         * @param command 客户端发送的命令
         */
        private void handleCommand(String command) {
            String[] parts = command.split("\\s+", 2);
            String cmd = parts[0].toUpperCase();
            String arg = parts.length > 1 ? parts[1] : null;

            try {
                switch (state) {
                    case AUTHORIZATION:
                        handleAuthorizationCommand(cmd, arg);
                        break;
                    case TRANSACTION:
                        handleTransactionCommand(cmd, arg);
                        break;
                    case UPDATE:
                        handleUpdateCommand(cmd, arg);
                        break;
                }
            } catch (Exception e) {
                log.error("处理POP3命令失败: {}", e.getMessage());
                sendResponse("-ERR 处理命令时发生错误");
            }
        }

        /**
         * 处理认证阶段命令
         */
        private void handleAuthorizationCommand(String cmd, String arg) {
            switch (cmd) {
                case "USER":
                    handleUserCommand(arg);
                    break;
                case "PASS":
                    handlePassCommand(arg);
                    break;
                case "QUIT":
                    handleQuitCommand();
                    break;
                default:
                    sendResponse("-ERR 未知命令");
            }
        }

        /**
         * 处理事务阶段命令
         */
        private void handleTransactionCommand(String cmd, String arg) {
            switch (cmd) {
                case "STAT":
                    handleStatCommand();
                    break;
                case "LIST":
                    handleListCommand(arg);
                    break;
                case "RETR":
                    handleRetrCommand(arg);
                    break;
                case "DELE":
                    handleDeleCommand(arg);
                    break;
                case "NOOP":
                    handleNoopCommand();
                    break;
                case "RSET":
                    handleRsetCommand();
                    break;
                case "QUIT":
                    handleQuitCommand();
                    break;
                default:
                    sendResponse("-ERR 未知命令");
            }
        }

        /**
         * 处理更新阶段命令
         */
        private void handleUpdateCommand(String cmd, String arg) {
            if ("QUIT".equals(cmd)) {
                handleQuitCommand();
            } else {
                sendResponse("-ERR 未知命令");
            }
        }

        /**
         * 处理USER命令
         */
        private void handleUserCommand(String username) {
            if (username == null || username.isEmpty()) {
                sendResponse("-ERR 用户名不能为空");
                return;
            }

            var userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                currentUser = username;
                sendResponse("+OK 用户存在");
            } else {
                sendResponse("-ERR 用户名不存在");
            }
        }

        /**
         * 处理PASS命令
         */
        private void handlePassCommand(String password) {
            if (currentUser == null) {
                sendResponse("-ERR 请先输入用户名");
                return;
            }

            if (password == null) {
                sendResponse("-ERR 密码不能为空");
                return;
            }

            var userOpt = userRepository.findByUsername(currentUser);
            if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
                // 认证成功，进入事务状态
                state = Pop3State.TRANSACTION;
                // 加载用户邮件
                loadUserMessages();
                sendResponse("+OK 认证成功");
            } else {
                sendResponse("-ERR 密码错误");
            }
        }

        /**
         * 处理STAT命令
         */
        private void handleStatCommand() {
            int messageCount = messageMap.size();
            long totalSize = messageMap.values().stream()
                    .mapToLong(mail -> mail.getContent().length())
                    .sum();
            sendResponse("+OK " + messageCount + " " + totalSize);
        }

        /**
         * 处理LIST命令
         */
        private void handleListCommand(String arg) {
            if (arg == null) {
                // LIST命令不带参数，返回所有邮件的列表
                int messageCount = messageMap.size();
                sendResponse("+OK " + messageCount + " messages");
                
                for (Map.Entry<Integer, Mail> entry : messageMap.entrySet()) {
                    int msgId = entry.getKey();
                    int size = entry.getValue().getContent().length();
                    sendResponse(msgId + " " + size);
                }
                sendResponse(".");
            } else {
                // LIST命令带参数，返回指定邮件的大小
                try {
                    int msgId = Integer.parseInt(arg);
                    Mail mail = messageMap.get(msgId);
                    if (mail != null) {
                        sendResponse("+OK " + msgId + " " + mail.getContent().length());
                    } else {
                        sendResponse("-ERR 邮件不存在");
                    }
                } catch (NumberFormatException e) {
                    sendResponse("-ERR 无效的邮件ID");
                }
            }
        }

        /**
         * 处理RETR命令
         */
        private void handleRetrCommand(String arg) {
            if (arg == null) {
                sendResponse("-ERR 请指定邮件ID");
                return;
            }

            try {
                int msgId = Integer.parseInt(arg);
                Mail mail = messageMap.get(msgId);
                
                if (mail != null) {
                    sendResponse("+OK 邮件内容 follows");
                    sendResponse("From: " + mail.getSender());
                    sendResponse("To: " + mail.getRecipient());
                    sendResponse("Subject: " + mail.getSubject());
                    sendResponse("Date: " + mail.getSentDate());
                    sendResponse("");
                    sendResponse(mail.getContent());
                    sendResponse(".");
                } else {
                    sendResponse("-ERR 邮件不存在");
                }
            } catch (NumberFormatException e) {
                sendResponse("-ERR 无效的邮件ID");
            }
        }

        /**
         * 处理DELE命令
         */
        private void handleDeleCommand(String arg) {
            if (arg == null) {
                sendResponse("-ERR 请指定邮件ID");
                return;
            }

            try {
                int msgId = Integer.parseInt(arg);
                if (messageMap.containsKey(msgId)) {
                    deletedMessages.put(msgId, true);
                    sendResponse("+OK 邮件已标记为删除");
                } else {
                    sendResponse("-ERR 邮件不存在");
                }
            } catch (NumberFormatException e) {
                sendResponse("-ERR 无效的邮件ID");
            }
        }

        /**
         * 处理NOOP命令
         */
        private void handleNoopCommand() {
            sendResponse("+OK");
        }

        /**
         * 处理RSET命令
         */
        private void handleRsetCommand() {
            deletedMessages.clear();
            sendResponse("+OK 删除标记已重置");
        }

        /**
         * 处理QUIT命令
         */
        private void handleQuitCommand() {
            sendResponse("+OK POP3服务器关闭连接");
            
            // 如果在事务状态，进入更新状态并处理删除操作
            if (state == Pop3State.TRANSACTION) {
                state = Pop3State.UPDATE;
                processDeletedMessages();
            }
            
            closeConnection();
        }

        /**
         * 发送响应给客户端
         */
        private void sendResponse(String response) {
            log.debug("发送POP3响应: {}", response);
            out.println(response);
        }

        /**
         * 加载用户邮件
         */
        private void loadUserMessages() {
            var mails = mailRepository.findByRecipient(currentUser);
            int msgId = 1;
            for (var mail : mails) {
                messageMap.put(msgId++, mail);
            }
        }

        /**
         * 处理已标记为删除的邮件
         */
        private void processDeletedMessages() {
            for (Map.Entry<Integer, Boolean> entry : deletedMessages.entrySet()) {
                if (entry.getValue()) {
                    Mail mail = messageMap.get(entry.getKey());
                    if (mail != null) {
                        mailRepository.delete(mail);
                        log.info("已删除邮件: {} (ID: {})", mail.getSubject(), entry.getKey());
                    }
                }
            }
        }

        /**
         * 关闭连接
         */
        private void closeConnection() {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (clientSocket != null) clientSocket.close();
                log.info("POP3客户端连接已关闭");
            } catch (IOException e) {
                log.error("关闭POP3客户端连接时出错: {}", e.getMessage());
            }
        }

        /**
         * POP3协议状态
         */
        private enum Pop3State {
            AUTHORIZATION, // 认证阶段
            TRANSACTION,   // 事务阶段
            UPDATE         // 更新阶段
        }
    }
}
