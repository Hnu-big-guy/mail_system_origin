package com.hnu.mail.config;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.hnu.mail.service.ServiceManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 应用程序启动监听器，用于初始化服务
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationStartupListener implements ApplicationListener<ContextRefreshedEvent> {

    private final ServiceManager serviceManager;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 初始化服务，包括启动POP3和SMTP服务器
        log.info("应用程序启动完成，开始初始化服务...");
        serviceManager.initializeServices();
        log.info("服务初始化完成");
    }
}
