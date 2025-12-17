package com.hnu.mail.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class ServerTest {
    private static final String SERVER_HOST = "localhost";
    private static final int POP3_PORT = 110;
    private static final int SMTP_PORT = 25;

    @Test
    void testPop3ServerConnection() throws IOException {
        try (Socket socket = new Socket(SERVER_HOST, POP3_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // 接收欢迎消息
            String response = in.readLine();
            assertNotNull(response);
            assertTrue(response.startsWith("+OK"), "POP3服务器未返回正确的欢迎消息");

            // 测试USER命令
            out.println("USER admin");
            response = in.readLine();
            assertNotNull(response);
            System.out.println("POP3 USER响应: " + response);

            // 测试PASS命令
            out.println("PASS admin123");
            response = in.readLine();
            assertNotNull(response);
            System.out.println("POP3 PASS响应: " + response);

            // 测试QUIT命令
            out.println("QUIT");
            response = in.readLine();
            assertNotNull(response);
            assertTrue(response.startsWith("+OK"), "POP3服务器未正确处理QUIT命令");
        }
    }

    @Test
    void testSmtpServerConnection() throws IOException {
        try (Socket socket = new Socket(SERVER_HOST, SMTP_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // 接收欢迎消息
            String response = in.readLine();
            assertNotNull(response);
            assertTrue(response.startsWith("220"), "SMTP服务器未返回正确的欢迎消息");

            // 测试EHLO命令
            out.println("EHLO localhost");
            List<String> responses = readSmtpMultiLineResponse(in);
            assertFalse(responses.isEmpty());
            System.out.println("SMTP EHLO响应:");
            responses.forEach(System.out::println);
            assertTrue(responses.get(responses.size() - 1).startsWith("250"), "SMTP服务器未正确处理EHLO命令");

            // 测试QUIT命令
            out.println("QUIT");
            response = in.readLine();
            assertNotNull(response);
            assertTrue(response.startsWith("221"), "SMTP服务器未正确处理QUIT命令");
        }
    }

    private List<String> readSmtpMultiLineResponse(BufferedReader in) throws IOException {
        List<String> responses = new ArrayList<>();
        String line;
        while ((line = in.readLine()) != null) {
            responses.add(line);
            if (!line.startsWith("250-")) {
                break;
            }
        }
        return responses;
    }
}
