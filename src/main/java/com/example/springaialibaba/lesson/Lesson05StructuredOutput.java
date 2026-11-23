package com.example.springaialibaba.lesson;

import com.example.springaialibaba.model.TravelPlan;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 课时 5：结构化输出（Structured Output）。
 * <p>
 * 运行：.\scripts\run.ps1 -Lesson 5
 * <p>
 * 学习要点：
 * 1. .entity(Class) 让模型按 Java 类的结构输出 JSON，并自动反序列化成对象。
 * 2. .responseEntity(...) 可以同时拿到原始 ChatResponse 和解析后的对象，便于调试。
 * 3. 字段名、类型、嵌套结构都会进入提示词，所以类定义本身就是“接口契约”。
 * 4. 输出 token 不足时 JSON 会被截断，常见报错是
 *    "Unexpected end-of-input: was expecting closing quote"，调大 max-tokens 即可。
 */
@Component
@Order(100)
@ConditionalOnProperty(prefix = "demo", name = "lesson", havingValue = "5")
public class Lesson05StructuredOutput implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(Lesson05StructuredOutput.class);

    private final ChatClient chatClient;

    public Lesson05StructuredOutput(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String requirement = """
                城市：杭州
                天数：2 天
                偏好：自然风光、咖啡、慢节奏
                总预算：1500 元人民币
                """;

        log.info("========== 课时 5：结构化输出 ==========");

        try {
            ResponseEntity<ChatResponse, TravelPlan> result = chatClient.prompt()
                    .system("""
                            你是资深中文旅行规划师。
                            请严格按照要求的 JSON 结构输出，不要输出 Markdown 代码块或额外解释。
                            """)
                    .user("请根据下面的需求生成旅行计划：\n" + requirement + """
                            
                            输出要求：
                            - title 不超过 15 个字
                            - 每天正好 2 个活动，每个活动不超过 20 个字
                            - highlights 正好 3 条，每条不超过 20 个字
                            - 预算为人民币整数
                            """)
                    .call()
                    .responseEntity(TravelPlan.class);

            TravelPlan plan = result.entity();
            String rawJson = result.response().getResult().getOutput().getText();

            log.info("[模型原始输出] {}", rawJson);
            log.info("[反序列化后的对象]\n{}",
                    new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(plan));
            log.info("[强类型访问] title={}, city={}, days={}, 预算={} 元, 第一天活动数={}",
                    plan.title(),
                    plan.city(),
                    plan.days(),
                    plan.budgetCny(),
                    plan.itinerary() == null ? 0 : plan.itinerary().get(0).activities().size());
        }
        catch (Exception e) {
            // 结构化输出失败时，把排查方向直接告诉学习者，而不是只抛 Jackson 堆栈
            log.error("结构化输出失败：{}", e.getMessage());
            log.error("最常见的两个原因：");
            log.error("  1) 输出被 max-tokens 截断 —— 把 spring.ai.deepseek.chat.options.max-tokens 调大（如 4096）");
            log.error("  2) 模型没有严格输出 JSON —— 加强 system prompt，或减少要求输出的内容量");
            log.error("可以用 .call().content() 打印原始文本，先确认模型到底输出了什么");
        }
        log.info("========== 课时 5 结束 ==========");
    }
}
