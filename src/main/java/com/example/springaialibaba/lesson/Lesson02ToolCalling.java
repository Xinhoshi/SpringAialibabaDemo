package com.example.springaialibaba.lesson;

import com.example.springaialibaba.tool.WeatherTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 课时 2：工具调用（Function Calling）。
 * <p>
 * 运行：mvn spring-boot:run "-Dspring-boot.run.arguments=--demo.lesson=2"
 * <p>
 * 学习要点：
 * 1. 普通 Java 方法 + @Tool 就能成为模型可调用的工具。
 * 2. .tools(bean) 把工具注册给本次请求；模型决定是否调用、调用哪个。
 * 3. 工具的执行结果会自动回传给模型，再由模型生成最终自然语言答案。
 */
@Component
@Order(100)
@ConditionalOnProperty(prefix = "demo", name = "lesson", havingValue = "2")
public class Lesson02ToolCalling implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(Lesson02ToolCalling.class);

    private final ChatClient chatClient;
    private final WeatherTools weatherTools;

    public Lesson02ToolCalling(ChatClient chatClient, WeatherTools weatherTools) {
        this.chatClient = chatClient;
        this.weatherTools = weatherTools;
    }

    @Override
    public void run(ApplicationArguments args) {
        String question = "北京今天天气怎么样？适合去跑步吗？";

        log.info("========== 课时 2：工具调用 ==========");
        log.info("[用户] {}", question);

        String answer = chatClient.prompt()
                .user(question)
                // 模型会看到 getWeather / isGoodForOutdoorSports 两个工具
                .tools(weatherTools)
                .call()
                .content();

        log.info("[模型] {}", answer);
        log.info("========== 课时 2 结束 ==========");
    }
}
