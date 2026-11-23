package com.example.springaialibaba.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 启动时打印环境检查信息，避免“跑起来了但不知道下一步做什么”。
 */
@Component
@Order(0)
public class LearningBanner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LearningBanner.class);

    @Value("${spring.ai.deepseek.api-key:}")
    private String apiKey;

    @Value("${demo.lesson:0}")
    private int lesson;

    @Override
    public void run(ApplicationArguments args) {
        log.info("");
        log.info("======================================================");
        log.info(" Spring AI Alibaba 学习工程已启动");
        log.info(" Web 接口:");
        log.info("   GET  http://localhost:8080/                    (课时 4 演示页)");
        log.info("   GET  http://localhost:8080/api/chat?message=你好");
        log.info("   GET  http://localhost:8080/api/plan?city=杭州   (结构化输出)");
        log.info("   POST http://localhost:8080/api/chat/stream   (SSE)");
        log.info("   POST http://localhost:8080/api/agent/chat    (ReactAgent)");
        log.info("   POST http://localhost:8080/api/agent/persistent/chat (持久化记忆)");
        log.info(" 当前课时: {}", lesson == 0 ? "未指定（仅 Web 服务）" : lesson);
        log.info("======================================================");

        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("sk-not-configured")) {
            log.warn("未检测到 DeepSeek API Key，真实模型调用会失败。");
            log.warn("PowerShell 设置方式: $env:DEEPSEEK_API_KEY=\"sk-你的Key\"");
            log.warn("Key 获取地址: https://platform.deepseek.com/api_keys");
        }
    }
}
