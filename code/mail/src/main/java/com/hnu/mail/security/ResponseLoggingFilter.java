package com.hnu.mail.security;

import org.springframework.stereotype.Component;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.*;

/**
 * 请求响应日志过滤器，用于打印前端传入的请求数据和后端返回的响应数据
 */
@Component
public class ResponseLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 包装请求对象以获取请求体
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper((HttpServletRequest) request);
        // 包装响应对象以获取响应体
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper((HttpServletResponse) response);
        
        try {
            // 打印请求信息
            printRequest(requestWrapper);
            // 继续过滤器链
            chain.doFilter(requestWrapper, responseWrapper);
        } finally {
            // 打印响应体
            printResponse(requestWrapper, responseWrapper);
            // 将缓存的响应内容写入原始响应
            responseWrapper.copyBodyToResponse();
        }
    }

    private void printRequest(ContentCachingRequestWrapper requestWrapper) throws IOException {
        // 获取请求信息
        String method = requestWrapper.getMethod();
        String uri = requestWrapper.getRequestURI();
        String queryString = requestWrapper.getQueryString();
        
        // 构建完整的请求URL
        StringBuilder fullUrl = new StringBuilder(method + " " + uri);
        if (queryString != null) {
            fullUrl.append("?").append(queryString);
        }
        
        System.out.println("\n[Request] " + fullUrl.toString());
        
        // 打印请求头
        HttpServletRequest httpRequest = requestWrapper;
        System.out.println("Headers:");
        httpRequest.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            System.out.println("  " + headerName + ": " + httpRequest.getHeader(headerName));
        });
        
        // 打印请求体
        byte[] requestBody = requestWrapper.getContentAsByteArray();
        if (requestBody.length > 0) {
            String contentType = requestWrapper.getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                String requestBodyStr = new String(requestBody, requestWrapper.getCharacterEncoding());
                System.out.println("Request Body: " + requestBodyStr);
            } else {
                System.out.println("Request Body: [Binary Data] (length: " + requestBody.length + " bytes)");
            }
        } else {
            System.out.println("Request Body: [Empty]");
        }
        System.out.println("----------------------------------------");
    }

    private void printResponse(ContentCachingRequestWrapper requestWrapper, ContentCachingResponseWrapper responseWrapper) throws IOException {
        // 获取请求信息
        String method = requestWrapper.getMethod();
        String uri = requestWrapper.getRequestURI();
        
        // 获取响应信息
        int status = responseWrapper.getStatus();
        String contentType = responseWrapper.getContentType();
        byte[] responseBody = responseWrapper.getContentAsByteArray();
        
        System.out.println("\n[Response] " + method + " " + uri);
        System.out.println("Status: " + status);
        
        // 打印响应头
        System.out.println("Headers:");
        responseWrapper.getHeaderNames().forEach(headerName -> {
            System.out.println("  " + headerName + ": " + responseWrapper.getHeader(headerName));
        });
        
        // 打印响应体
        if (responseBody.length > 0) {
            if (contentType != null && contentType.contains("application/json")) {
                String responseBodyStr = new String(responseBody, responseWrapper.getCharacterEncoding());
                System.out.println("Response Body: " + responseBodyStr);
            } else {
                System.out.println("Response Body: [Binary Data] (length: " + responseBody.length + " bytes)");
            }
        } else {
            System.out.println("Response Body: [Empty]");
        }
        System.out.println("----------------------------------------");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化方法
    }

    @Override
    public void destroy() {
        // 销毁方法
    }
}