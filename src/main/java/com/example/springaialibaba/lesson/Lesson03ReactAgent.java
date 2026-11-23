package com.example.springaialibaba.lesson;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 课时 3：ReactAgent（对应官方 Quick Start 的核心用法）。
 * <p>
 * 运行：mvn spring-boot:run "-Dspring-boot.run.arguments=--demo.lesson=3"
 * <p>
 * 学习要点：
 * 1. ReactAgent = 模型 + 系统提示词 + 工具 + 记忆（CheckpointSaver）。
 * 2. threadId 标识一段会话；相同 threadId 的多次 call 会自动带上历史消息。
 * 3. Agent 会自行完成 “思考 → 调用工具 → 观察结果 → 再回答” 的循环。
 */
@Component
@Order(100)
@ConditionalOnProperty(prefix = "demo", name = "lesson", havingValue = "3")
public class Lesson03ReactAgent implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(Lesson03ReactAgent.class);

    private final ReactAgent weatherAgent;

    public Lesson03ReactAgent(@Qualifier("weatherAgent") ReactAgent weatherAgent) {
        this.weatherAgent = weatherAgent;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 每次启动都用新的 threadId，避免上一次运行的历史干扰演示
        String threadId = "lesson-03-" + System.currentTimeMillis();
        RunnableConfig runnableConfig = RunnableConfig.builder()
                .threadId(threadId)
                .addMetadata("user_id", "demo-user")
                .build();

        log.info("========== 课时 3：ReactAgent ==========");
        log.info("[threadId] {}", threadId);

        String firstQuestion = "北京今天天气怎么样？";
        log.info("[用户] {}", firstQuestion);
        AssistantMessage first = weatherAgent.call(firstQuestion, runnableConfig);
        log.info("[Agent] {}", first.getText());

        // 第二轮不重复城市，验证 Agent 是否记住了上下文
        String secondQuestion = "那适合去跑步吗？";
        log.info("[用户] {}", secondQuestion);
        AssistantMessage second = weatherAgent.call(secondQuestion, runnableConfig);
        log.info("[Agent] {}", second.getText());

        log.info("========== 课时 3 结束 ==========");
    }
}
