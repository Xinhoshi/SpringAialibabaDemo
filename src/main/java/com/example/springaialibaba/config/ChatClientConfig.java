package com.example.springaialibaba.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 统一构建一个带默认系统提示词的 ChatClient。
 * <p>
 * ChatClient 是 Spring AI 的高层对话 API，底层仍然使用 ChatModel（这里由
 * spring-ai-alibaba-starter-dashscope 自动配置为 DashScopeChatModel）。
 */
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        你是一位乐于助人的中文 AI 助手。
                        回答使用简体中文，准确、简洁；不确定时明确说明，不要编造。
                        如果问题不清楚，请先提出一个澄清问题。
                        """)
                .build();
    }
}
