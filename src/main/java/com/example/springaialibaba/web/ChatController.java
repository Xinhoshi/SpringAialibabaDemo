package com.example.springaialibaba.web;

import java.util.List;
import java.util.UUID;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.example.springaialibaba.model.TravelPlan;
import com.example.springaialibaba.rag.RagAnswer;
import com.example.springaialibaba.rag.RagService;
import com.example.springaialibaba.web.dto.ChatReply;
import com.example.springaialibaba.web.dto.ChatRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 把各课时的能力暴露成 HTTP 接口，方便用浏览器 / curl / 前端联调。
 */
@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatClient chatClient;
    private final ReactAgent weatherAgent;
    private final ReactAgent persistentWeatherAgent;
    private final RagService ragService;

    public ChatController(ChatClient chatClient,
            @Qualifier("weatherAgent") ReactAgent weatherAgent,
            @Qualifier("persistentWeatherAgent") ReactAgent persistentWeatherAgent,
            RagService ragService) {
        this.chatClient = chatClient;
        this.weatherAgent = weatherAgent;
        this.persistentWeatherAgent = persistentWeatherAgent;
        this.ragService = ragService;
    }

    /** 课时 1：GET /api/chat?message=你好 */
    @GetMapping("/chat")
    public ChatReply chat(@RequestParam("message") String message) {
        requireText(message);
        String answer = chatClient.prompt().user(message).call().content();
        return new ChatReply(answer, null);
    }

    /** 课时 2：流式输出（SSE），需要 POST，因为请求体里有消息 */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestBody ChatRequest request) {
        requireText(request.message());
        return chatClient.prompt()
                .user(request.message())
                .stream()
                .content();
    }

    /** 课时 3：带工具与内存记忆的 ReactAgent。conversationId 对应 threadId */
    @PostMapping("/agent/chat")
    public ChatReply agentChat(@RequestBody ChatRequest request) throws Exception {
        return doAgentChat(weatherAgent, request);
    }

    /** 课时 6：带文件持久化记忆的 ReactAgent，进程重启后仍能恢复同一 threadId 的对话 */
    @PostMapping("/agent/persistent/chat")
    public ChatReply persistentAgentChat(@RequestBody ChatRequest request) throws Exception {
        return doAgentChat(persistentWeatherAgent, request);
    }

    /** 课时 5：结构化输出，直接返回 Java 对象对应的 JSON */
    @GetMapping("/plan")
    public TravelPlan plan(
            @RequestParam("city") String city,
            @RequestParam(name = "days", defaultValue = "2") int days,
            @RequestParam(name = "budget", defaultValue = "1500") int budget,
            @RequestParam(name = "preference", defaultValue = "自然风光、咖啡、慢节奏") String preference) {

        requireText(city);
        String prompt = "请根据下面的需求生成旅行计划：\n"
                + "城市：" + city + "\n"
                + "天数：" + days + " 天\n"
                + "偏好：" + preference + "\n"
                + "总预算：" + budget + " 元人民币\n"
                + "要求：预算为人民币整数；每天安排 2-3 个活动；highlights 正好 3 条。";

        return chatClient.prompt()
                .system("""
                        你是资深中文旅行规划师。
                        请严格按照要求的 JSON 结构输出，不要输出 Markdown 代码块或额外解释。
                        """)
                .user(prompt)
                .call()
                .entity(TravelPlan.class);
    }

    /** 课时 7：RAG 问答，返回答案与命中的知识来源 */
    @GetMapping("/rag")
    public RagAnswer rag(@RequestParam("question") String question) {
        requireText(question);
        return ragService.ask(question);
    }

    /** 课时 7：只做向量检索，不调用大模型，便于观察命中结果 */
    @GetMapping("/rag/search")
    public List<RagAnswer.Source> ragSearch(
            @RequestParam("question") String question,
            @RequestParam(name = "topK", defaultValue = "4") int topK) {
        requireText(question);
        return ragService.search(question, topK);
    }

    private ChatReply doAgentChat(ReactAgent agent, ChatRequest request) throws Exception {
        requireText(request.message());
        String threadId = StringUtils.hasText(request.conversationId())
                ? request.conversationId()
                : UUID.randomUUID().toString();

        RunnableConfig runnableConfig = RunnableConfig.builder()
                .threadId(threadId)
                .addMetadata("user_id", "web-user")
                .build();

        AssistantMessage message = agent.call(request.message(), runnableConfig);
        return new ChatReply(message.getText(), threadId);
    }

    private static void requireText(String message) {
        if (!StringUtils.hasText(message)) {
            throw new IllegalArgumentException("message 不能为空");
        }
    }
}
