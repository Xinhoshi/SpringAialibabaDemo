package com.example.springaialibaba.lesson;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 课时 4：流式输出（Streaming）。
 * <p>
 * 运行：.\scripts\run.ps1 -Lesson 4
 * <p>
 * 学习要点：
 * 1. .call() 会等模型全部生成完才返回；.stream() 会在生成过程中不断返回增量（chunk）。
 * 2. .stream().content() 返回的是 Reactor 的 Flux&lt;String&gt;，天然适合 SSE / WebSocket。
 * 3. 流式输出只改善“感知延迟”，不减少总耗时；首字到达时间（TTFT）才是关键指标。
 * 4. 注意：这里用 blockLast 是为了在控制台顺序打印，Web 场景不要阻塞线程。
 */
@Component
@Order(100)
@ConditionalOnProperty(prefix = "demo", name = "lesson", havingValue = "4")
public class Lesson04Streaming implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(Lesson04Streaming.class);

    private final ChatClient chatClient;

    public Lesson04Streaming(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        String question = "用 5 句话介绍流式输出给聊天应用带来的体验提升。";

        log.info("========== 课时 4：流式输出 ==========");
        log.info("[用户] {}", question);
        log.info("[模型] 下面的内容会按 chunk 实时打印：");

        AtomicInteger chunkCount = new AtomicInteger();
        StringBuilder fullText = new StringBuilder();
        long start = System.currentTimeMillis();

        try {
            chatClient.prompt()
                    .user(question)
                    .stream()
                    .content()
                    .doOnNext(chunk -> {
                        chunkCount.incrementAndGet();
                        fullText.append(chunk);
                        // 直接 print + flush，才能看到“一个字一个字蹦出来”的效果
                        System.out.print(chunk);
                        System.out.flush();
                    })
                    .blockLast(Duration.ofSeconds(120));

            System.out.println();
            long cost = System.currentTimeMillis() - start;
            log.info("[统计] 共收到 {} 个 chunk，总字符数 {}，总耗时 {} ms",
                    chunkCount.get(), fullText.length(), cost);
            log.info("提示：对比课时 1 的 .call()，总耗时相近，但首字出现得早得多。");
        }
        catch (Exception e) {
            // 学习工程里不因为一次调用失败就退出应用，方便继续改配置重试
            log.error("流式调用失败：{}", e.getMessage());
        }
        log.info("========== 课时 4 结束 ==========");
    }
}
