package com.hnu.mail.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hnu.mail.model.Mail;
import com.hnu.mail.model.SystemConfig;
import com.hnu.mail.repository.MailRepository;
import com.hnu.mail.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SmtpServer {

    @Autowired
    private SystemConfigService configService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailRepository mailRepository;

    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private boolean running = false;

    /**
     * 启动SMTP服务器
     */
    public void start() {
        if (running) {
            log.info("SMTP服务器已经在运行中");
            return;
        }

        try {
            // 从系统配置获取端口、启用状态和域名
            int port = Integer.parseInt(configService.getConfigValue(SystemConfig.SMTP_SERVER_PORT, "25"));
            boolean enabled = Boolean.parseBoolean(configService.getConfigValue(SystemConfig.SMTP_SERVER_ENABLED, "true"));
            String domain = configService.getConfigValue(SystemConfig.SMTP_SERVER_DOMAIN, "localhost");
            
            if (!enabled) {
                log.info("SMTP服务器已被禁用");
                return;
            }
            
            serverSocket = new ServerSocket(port);
            executorService = Executors.newCachedThreadPool();
            running = true;

            log.info("SMTP服务器已启动，监听端口: {}", port);

            // 启动线程监听客户端连接
            new Thread(() -> {
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        log.info("新的SMTP客户端连接: {}", clientSocket.getInetAddress());
                        executorService.submit(new SmtpClientHandler(clientSocket));
                    } catch (IOException e) {
                        if (running) {
                            log.error("SMTP服务器接受连接失败: {}", e.getMessage());
                        }
                    }
                }
            }).start();

        } catch (IOException e) {
            log.error("启动SMTP服务器失败: {}", e.getMessage());
            running = false;
        }
    }

    /**
     * 停止SMTP服务器
     */
    public void stop() {
        if (!running) {
            log.info("SMTP服务器已经停止");
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

            log.info("SMTP服务器已停止");
        } catch (IOException e) {
            log.error("停止SMTP服务器失败: {}", e.getMessage());
        }
    }

    /**
     * 重启SMTP服务器
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
     * 检查SMTP服务器是否正在运行
     * @return 服务器运行状态
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * SMTP客户端处理器
     */
    private class SmtpClientHandler implements Runnable {

        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private SmtpState state;
        private String sender;
        private List<String> recipients;
        private StringBuilder messageContent;
        private boolean dataMode;

        public SmtpClientHandler(Socket socket) {
            this.clientSocket = socket;
            this.state = SmtpState.CONNECTION;
            this.recipients = new ArrayList<>();
            this.messageContent = new StringBuilder();
            this.dataMode = false;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // 发送欢迎消息
                String domain = configService.getConfigValue(SystemConfig.SMTP_SERVER_DOMAIN, "localhost");
                sendResponse("220 " + domain + " SMTP Server Ready"); 

                String line;
                while ((line = in.readLine()) != null) {
                    log.debug("收到SMTP命令: {}", line);
                    
                    if (dataMode) {
                        handleDataLine(line);
                    } else {
                        handleCommand(line.trim());
                    }
                }
            } catch (IOException e) {
                log.error("处理SMTP客户端连接时出错: {}", e.getMessage());
            } finally {
                closeConnection();
            }
        }

        /**
         * 处理SMTP命令
         * @param command 客户端发送的命令
         */
        private void handleCommand(String command) {
            String[] parts = command.split("\\s+", 2);
            String cmd = parts[0].toUpperCase();
            String arg = parts.length > 1 ? parts[1] : null;

            try {
                switch (state) {
                    case CONNECTION:
                        if ("EHLO".equals(cmd) || "HELO".equals(cmd)) {
                            handleEhloHelo(cmd, arg);
                            state = SmtpState.MAIL;
                        } else {
                            sendResponse("503 请先发送EHLO或HELO命令");
                        }
                        break;
                    case MAIL:
                        if ("MAIL".equals(cmd)) {
                            handleMailFrom(arg);
                            state = SmtpState.RCPT;
                        } else if ("RSET".equals(cmd)) {
                            handleRset();
                        } else if ("QUIT".equals(cmd)) {
                            handleQuit();
                        } else {
                            sendResponse("503 请先发送MAIL FROM命令");
                        }
                        break;
                    case RCPT:
                        if ("RCPT".equals(cmd)) {
                            handleRcptTo(arg);
                        } else if ("DATA".equals(cmd)) {
                            if (recipients.isEmpty()) {
                                sendResponse("503 请先发送RCPT TO命令");
                            } else {
                                handleData();
                            }
                        } else if ("RSET".equals(cmd)) {
                            handleRset();
                        } else if ("QUIT".equals(cmd)) {
                            handleQuit();
                        } else {
                            sendResponse("503 请先发送RCPT TO或DATA命令");
                        }
                        break;
                    case DATA:
                        // 数据模式下的命令处理在handleDataLine中
                        break;
                }
            } catch (Exception e) {
                log.error("处理SMTP命令失败: {}", e.getMessage());
                sendResponse("500 处理命令时发生错误");
            }
        }

        /**
         * 处理数据模式下的行
         */
        private void handleDataLine(String line) {
            if (".".equals(line)) {
                // 数据结束标记
                dataMode = false;
                state = SmtpState.MAIL;
                
                // 保存邮件
                saveMail();
                
                // 清空数据
                messageContent.setLength(0);
                
                sendResponse("250 OK 邮件已接收");
            } else {
                // 处理转义的点
                if (line.startsWith(".")) {
                    line = line.substring(1);
                }
                messageContent.append(line).append("\n");
            }
        }

        /**
         * 处理EHLO/HELO命令
         */
        private void handleEhloHelo(String cmd, String arg) {
            if (arg == null || arg.isEmpty()) {
                sendResponse("501 参数不能为空");
                return;
            }
            
            // 从系统配置获取域名
            String domain = configService.getConfigValue(SystemConfig.SMTP_SERVER_DOMAIN, "localhost");

            if ("EHLO".equals(cmd)) {
                // EHLO命令，支持扩展SMTP
                sendResponse("250-" + domain);
                sendResponse("250-SIZE 10485760"); // 支持10MB邮件
                sendResponse("250-8BITMIME");
                sendResponse("250-PIPELINING");
                sendResponse("250 HELP");
            } else {
                // HELO命令，基本SMTP
                sendResponse("250 " + domain + " Hello " + arg);
            }
        }

        /**
         * 处理MAIL FROM命令
         */
        private void handleMailFrom(String arg) {
            if (arg == null || !arg.startsWith("FROM:")) {
                sendResponse("501 无效的MAIL FROM命令");
                return;
            }

            // 提取发件人地址
            String fromAddress = arg.substring(5).trim();
            // 移除尖括号
            if (fromAddress.startsWith("<")) {
                fromAddress = fromAddress.substring(1, fromAddress.length() - 1);
            }

            this.sender = fromAddress;
            sendResponse("250 OK");
        }

        /**
         * 处理RCPT TO命令
         */
        private void handleRcptTo(String arg) {
            if (arg == null || !arg.startsWith("TO:")) {
                sendResponse("501 无效的RCPT TO命令");
                return;
            }

            // 提取收件人地址
            String toAddress = arg.substring(3).trim();
            // 移除尖括号
            if (toAddress.startsWith("<")) {
                toAddress = toAddress.substring(1, toAddress.length() - 1);
            }

            // 检查收件人是否存在
            String username = extractUsername(toAddress);
            var userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                recipients.add(toAddress);
                sendResponse("250 OK");
            } else {
                sendResponse("550 收件人不存在");
            }
        }

        /**
         * 处理DATA命令
         */
        private void handleData() {
            dataMode = true;
            state = SmtpState.DATA;
            sendResponse("354 开始邮件输入，以.结束");
        }

        /**
         * 处理RSET命令
         */
        private void handleRset() {
            sender = null;
            recipients.clear();
            messageContent.setLength(0);
            dataMode = false;
            state = SmtpState.MAIL;
            sendResponse("250 OK");
        }

        /**
         * 处理QUIT命令
         */
        private void handleQuit() {
            // 从系统配置获取域名
            String domain = configService.getConfigValue(SystemConfig.SMTP_SERVER_DOMAIN, "localhost");
            sendResponse("221 " + domain + " SMTP Server Closing Connection");
            closeConnection();
        }

        /**
         * 处理NOOP命令
         */
        private void handleNoop() {
            sendResponse("250 OK");
        }

        /**
         * 处理VRFY命令
         */
        private void handleVrfy(String arg) {
            if (arg == null || arg.isEmpty()) {
                sendResponse("501 参数不能为空");
                return;
            }

            String username = extractUsername(arg);
            if (userRepository.existsByUsername(username)) {
                sendResponse("252 无法验证，但将尝试投递");
            } else {
                sendResponse("550 收件人不存在");
            }
        }

        /**
         * 发送响应给客户端
         */
        private void sendResponse(String response) {
            log.debug("发送SMTP响应: {}", response);
            out.println(response);
        }

        /**
         * 保存邮件到数据库
         */
        private void saveMail() {
            String content = messageContent.toString();
            
            // 提取主题
            String subject = extractSubject(content);
            
            // 为每个收件人创建邮件
            for (String recipient : recipients) {
                String username = extractUsername(recipient);
                
                // 查找用户
                var userOpt = userRepository.findByUsername(username);
                if (userOpt.isEmpty()) {
                    log.error("收件人不存在: {}", username);
                    continue;
                }
                
                Mail mail = new Mail();
                mail.setSenderEmail(sender); // 设置发件人邮箱地址
                mail.setReceiverEmail(recipient); // 设置收件人邮箱地址
                mail.setReceiver(userOpt.get()); // 设置收件人用户对象
                mail.setSubject(subject);
                mail.setContent(content);
                mail.setSentAt(java.time.LocalDateTime.now());
                mail.setReceivedAt(java.time.LocalDateTime.now());
                mail.setFolder(Mail.MailFolder.INBOX);
                mail.setIsRead(false);
                
                // 计算邮件大小
                mail.setSize((content.length() + 1023) / 1024); // 转换为KB
                
                mailRepository.save(mail);
                log.info("已保存邮件: {} 从 {} 到 {}", subject, sender, username);
            }
        }

        /**
         * 从邮件地址中提取用户名
         */
        private String extractUsername(String emailAddress) {
            int atIndex = emailAddress.indexOf('@');
            if (atIndex > 0) {
                return emailAddress.substring(0, atIndex);
            }
            return emailAddress;
        }

        /**
         * 从邮件内容中提取主题
         */
        private String extractSubject(String content) {
            String[] lines = content.split("\n");
            for (String line : lines) {
                if (line.toLowerCase().startsWith("subject:")) {
                    return line.substring(8).trim();
                }
            }
            return "无主题";
        }

        /**
         * 关闭连接
         */
        private void closeConnection() {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (clientSocket != null) clientSocket.close();
                log.info("SMTP客户端连接已关闭");
            } catch (IOException e) {
                log.error("关闭SMTP客户端连接时出错: {}", e.getMessage());
            }
        }

        /**
         * SMTP协议状态
         */
        private enum SmtpState {
            CONNECTION, // 连接阶段
            MAIL,       // 等待MAIL FROM命令
            RCPT,       // 等待RCPT TO命令
            DATA        // 数据传输阶段
        }
    }
}
