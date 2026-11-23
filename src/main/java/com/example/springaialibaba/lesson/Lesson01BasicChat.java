package com.example.springaialibaba.lesson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 课时 1：最基础的同步对话。
 * <p>
 * 运行：mvn spring-boot:run "-Dspring-boot.run.arguments=--demo.lesson=1"
 * <p>
 * 学习要点：
 * 1. ChatClient 负责“组织提示词 + 调用模型 + 返回结果”。
 * 2. prompt().user(...).call() 是同步阻塞调用。
 * 3. chatResponse() 除了文本，还能拿到 token 用量等元数据。
 */
@Component
@Order(100)
@ConditionalOnProperty(prefix = "demo", name = "lesson", havingValue = "1")
public class Lesson01BasicChat implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(Lesson01BasicChat.class);

    private final ChatClient chatClient;

    public Lesson01BasicChat(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        String question = "用三句话解释 Spring AI 和 Spring AI Alibaba 的关系。";

        log.info("========== 课时 1：基础对话 ==========");
        log.info("[用户] {}", question);

        ChatResponse response = chatClient.prompt()
                .user(question)
                .call()
                .chatResponse();

        log.info("[模型] {}", response.getResult().getOutput().getText());

        Usage usage = response.getMetadata() == null ? null : response.getMetadata().getUsage();
        if (usage != null) {
            log.info("[用量] promptTokens={}, completionTokens={}, totalTokens={}",
                    usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
        }
        log.info("========== 课时 1 结束 ==========");
    }
}
