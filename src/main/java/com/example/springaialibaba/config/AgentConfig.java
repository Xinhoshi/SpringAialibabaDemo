package com.example.springaialibaba.config;

import java.nio.file.Path;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.BaseCheckpointSaver;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.checkpoint.savers.file.FileSystemSaver;
import com.alibaba.cloud.ai.graph.serializer.std.SpringAIStateSerializer;
import com.example.springaialibaba.tool.WeatherTools;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ReactAgent 是 Spring AI Alibaba Agent Framework 的核心。
 * <p>
 * 它与 ChatClient 的区别：
 * <ul>
 *   <li>ChatClient：一次请求 → 一次（可能带工具调用的）模型响应</li>
 *   <li>ReactAgent：一个可循环的 Agent 图（思考 → 调工具 → 观察 → 再思考），
 *       并且自带基于 threadId 的对话状态（配合 CheckpointSaver）。</li>
 * </ul>
 */
@Configuration
public class AgentConfig {

    private static final String WEATHER_SYSTEM_PROMPT = """
            你是一位专业的中文天气助手，说话简洁友好。

            你可以使用以下工具：
            - getWeather：查询某个城市今天的天气
            - isGoodForOutdoorSports：判断某个城市明天是否适合户外运动

            规则：
            1. 用户问天气但没给城市时，先用一句话反问城市，不要编造。
            2. 需要天气数据时必须调用工具，不要凭记忆回答。
            3. 最终回答控制在 3 句话以内。
            """;

    @Value("${demo.checkpoint-dir:./data/checkpoints}")
    private String checkpointDir;

    /**
     * 课时 3：内存版记忆。进程重启后对话丢失。
     */
    @Bean
    public ReactAgent weatherAgent(ChatModel chatModel, WeatherTools weatherTools) {
        return ReactAgent.builder()
                .name("weather_agent")
                .description("中文天气助手，可以查询天气并给出出行建议")
                .model(chatModel)
                .systemPrompt(WEATHER_SYSTEM_PROMPT)
                // 自动扫描 weatherTools 中所有 @Tool 方法
                .methodTools(weatherTools)
                // 内存版检查点：同一个 threadId 的对话会被记住（进程重启后丢失）
                .saver(new MemorySaver())
                .build();
    }

    /**
     * 课时 6：文件版记忆。检查点写到 data/checkpoints，进程重启后仍可恢复。
     */
    @Bean
    public ReactAgent persistentWeatherAgent(ChatModel chatModel, WeatherTools weatherTools) {
        BaseCheckpointSaver fileSaver = FileSystemSaver.builder()
                .targetFolder(Path.of(checkpointDir))
                .stateSerializer(new SpringAIStateSerializer())
                .build();

        return ReactAgent.builder()
                .name("persistent_weather_agent")
                .description("带持久化记忆的中文天气助手")
                .model(chatModel)
                .systemPrompt(WEATHER_SYSTEM_PROMPT)
                .methodTools(weatherTools)
                .saver(fileSaver)
                .build();
    }
}
