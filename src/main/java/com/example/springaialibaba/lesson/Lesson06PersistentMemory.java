package com.example.springaialibaba.lesson;

import java.nio.file.Files;
import java.nio.file.Path;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.file.FileSystemSaver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 课时 6：持久化记忆（FileSystemSaver）。
 * <p>
 * 第一次运行：\scripts\run.ps1 -Lesson 6
 * 停止后再次运行同一条命令，验证 Agent 仍记得上一轮的对话。
 * <p>
 * 学习要点：
 * 1. MemorySaver 只存在内存里；FileSystemSaver 会把检查点写到磁盘。
 * 2. 记忆的隔离键是 threadId：同一个 threadId = 同一段会话。
 * 3. 生产环境可换成 RedisSaver / MysqlSaver / PostgresSaver，用法同样是 .saver(...)。
 */
@Component
@Order(100)
@ConditionalOnProperty(prefix = "demo", name = "lesson", havingValue = "6")
public class Lesson06PersistentMemory implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(Lesson06PersistentMemory.class);

    /** 固定 threadId，保证每次重启后还是同一段会话 */
    private static final String THREAD_ID = "lesson-06-persistent";

    private final ReactAgent persistentAgent;

    @Value("${demo.checkpoint-dir:./data/checkpoints}")
    private String checkpointDir;

    public Lesson06PersistentMemory(
            @Qualifier("persistentWeatherAgent") ReactAgent persistentAgent) {
        this.persistentAgent = persistentAgent;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("========== 课时 6：持久化记忆 ==========");
        log.info("[threadId] {}", THREAD_ID);

        // 直接检查磁盘上的检查点文件：应用刚启动、还没执行 call 时，
        // getThreadState 可能读不到磁盘状态，所以用文件存在性判断最直观。
        Path saverFile = Path.of(checkpointDir, "thread-" + THREAD_ID + FileSystemSaver.EXTENSION);
        boolean hasHistory = Files.exists(saverFile);

        log.info("[检查点文件] {}", saverFile.toAbsolutePath());

        if (!hasHistory) {
            log.info("[首次运行] 没有历史记忆，先进行两次对话写入记忆");
            log.info("[用户] 我叫小明，请记住我的名字。");
            AssistantMessage intro = persistentAgent.call(
                    "我叫小明，请记住我的名字。", config());
            log.info("[Agent] {}", intro.getText());

            log.info("[用户] 北京今天天气怎么样？");
            AssistantMessage weather = persistentAgent.call(
                    "北京今天天气怎么样？", config());
            log.info("[Agent] {}", weather.getText());

            log.info("提示：现在停止应用（Ctrl+C），再执行一次同样的命令。");
        }
        else {
            log.info("[检测到历史记忆] 检查点文件已存在，本次不重复写入，直接验证旧记忆");
        }

        log.info("[用户] 我叫什么名字？我之前问过哪个城市的天气？");
        AssistantMessage recall = persistentAgent.call(
                "我叫什么名字？我之前问过哪个城市的天气？", config());
        log.info("[Agent] {}", recall.getText());

        log.info("========== 课时 6 结束 ==========");
    }

    private static RunnableConfig config() {
        return RunnableConfig.builder()
                .threadId(THREAD_ID)
                .addMetadata("user_id", "demo-user")
                .build();
    }
}
