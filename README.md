# Spring AI Alibaba 学习工程

一个**按课时递进**的 Spring AI Alibaba 示例工程：先跑通最小对话，再逐步加上工具调用、Agent、记忆等能力。

当前已实现 7 个课时，每个课时都可以单独启动运行：

| 课时 | 内容 | 核心 API |
| --- | --- | --- |
| 1 | 基础对话 | `ChatClient.prompt().call()` |
| 2 | 工具调用（Function Calling） | `@Tool` + `.tools(...)` |
| 3 | ReactAgent + 多轮记忆 | `ReactAgent` + `RunnableConfig.threadId` + `MemorySaver` |
| 4 | 流式输出（SSE）与浏览器演示 | `.stream().content()` + `Flux<String>` |
| 5 | 结构化输出 | `.entity(Class)` / `.responseEntity(...)` |
| 6 | 持久化记忆 | `FileSystemSaver` + `threadId` |
| 7 | RAG 检索增强生成 | `EmbeddingModel` + `VectorStore` + 文档切块 |

## 技术栈

- JDK 17+（本项目在 JDK 17 上验证）
- Spring Boot 3.5.10
- Spring AI 1.1.2
- Spring AI Alibaba 1.1.2.3
- 模型：DeepSeek `deepseek-flash`（默认）；当前账号还可用 `deepseek-v4-pro`

## 目录结构

```
SpringAialibabaDemo
├─ pom.xml
├─ scripts/run.ps1                     # 一键运行脚本（自动选 JDK17 + Maven3.8+）
├─ docs/LEARNING-PATH.md               # 后续学习路线
└─ src/main
   ├─ java/com/example/springaialibaba
   │  ├─ SpringAiAlibabaDemoApplication.java
   │  ├─ config
   │  │  ├─ ChatClientConfig.java      # 构建带默认系统提示词的 ChatClient
   │  │  ├─ AgentConfig.java           # 构建 ReactAgent（模型 + 工具 + 记忆）
   │  │  └─ LearningBanner.java        # 启动时打印接口与 Key 检查
   │  ├─ lesson
   │  │  ├─ Lesson01BasicChat.java     # 课时 1
   │  │  ├─ Lesson02ToolCalling.java   # 课时 2
   │  │  ├─ Lesson03ReactAgent.java    # 课时 3
   │  │  ├─ Lesson04Streaming.java     # 课时 4
   │  │  ├─ Lesson05StructuredOutput.java # 课时 5
   │  │  ├─ Lesson06PersistentMemory.java # 课时 6
   │  │  └─ Lesson07Rag.java            # 课时 7
   │  ├─ embedding/LocalHashingEmbeddingModel.java # 本地离线 embedding
   │  ├─ rag
   │  │  ├─ KnowledgeBase.java         # 加载/切块/写入向量库
   │  │  ├─ RagService.java            # 检索 + 拼上下文 + 生成
   │  │  └─ RagAnswer.java
   │  ├─ model/TravelPlan.java         # 结构化输出的目标类型
   │  ├─ tool/WeatherTools.java        # @Tool 工具示例（模拟天气数据）
   │  └─ web
   │     ├─ ChatController.java        # REST / SSE 接口
   │     ├─ ApiExceptionHandler.java
   │     └─ dto/...
   └─ resources
      ├─ application.yml
      ├─ application-local.yml         # 本地 Key（已 gitignore）
      ├─ knowledge/*.md                # 课时 7 的知识库
      └─ static/index.html             # 课时 4 浏览器演示页
```

运行课时 6 后会生成 `data/checkpoints/`，里面是持久化检查点（已 gitignore）。

## 一、准备 API Key

到 [DeepSeek 开放平台](https://platform.deepseek.com/api_keys) 创建 API Key。

PowerShell 中设置（只对当前窗口有效）：

```powershell
$env:DEEPSEEK_API_KEY = "sk-你的Key"
```

也可以永久写入用户环境变量（新开终端生效）：

```powershell
[Environment]::SetEnvironmentVariable("DEEPSEEK_API_KEY", "sk-你的Key", "User")
```

> 正式使用时建议用环境变量，不要把 Key 写进 `application.yml` 后提交到仓库。

本项目已经激活 `local` profile（`application.yml` 中 `spring.profiles.active: local`），也可以直接把 Key 写到 `src/main/resources/application-local.yml`（已在 `.gitignore` 中忽略）：

```yaml
spring:
  ai:
    deepseek:
      api-key: sk-你的Key
```

`application.yml` 中通过 `${DEEPSEEK_API_KEY:sk-not-configured}` 读取环境变量，`application-local.yml` 会覆盖它：

```yaml
spring:
  ai:
    deepseek:
      api-key: ${DEEPSEEK_API_KEY:sk-not-configured}
      chat:
        options:
          model: deepseek-flash
```

### 可选：切换模型 / 提供商

以 `GET https://api.deepseek.com/models` 的返回为准，当前账号可用：

| 模型 | 特点 | 适用课时 |
| --- | --- | --- |
| `deepseek-flash` | 响应快，通用对话与工具调用（默认） | 全部 |
| `deepseek-v4-pro` | 能力更强，成本 / 延迟更高 | 全部 |

改模型只需要改 `application.yml`：

```yaml
spring:
  ai:
    deepseek:
      chat:
        options:
          model: deepseek-v4-pro
```

如果你使用 `deepseek-reasoner` 这类推理模型，注意它**不支持 Function Calling**，课时 2/3 会失败。

切回通义千问（DashScope）：

1. `pom.xml` 中注释 `spring-ai-starter-model-deepseek`，放开 `spring-ai-alibaba-starter-dashscope`；
2. `application.yml` 改成 `spring.ai.dashscope.api-key` / `spring.ai.dashscope.chat.options.model`；
3. 设置环境变量 `AI_DASHSCOPE_API_KEY`。

> Spring AI Alibaba 的 Agent Framework 只依赖 `ChatModel` 抽象，所以换模型提供商不需要改业务代码。

## 二、运行

### 方式 A：使用脚本（推荐）

```powershell
# 只启动 Web 服务
.\scripts\run.ps1

# 运行课时 1 / 2 / 3 / 4 / 5 / 6 / 7
.\scripts\run.ps1 -Lesson 1
.\scripts\run.ps1 -Lesson 2
.\scripts\run.ps1 -Lesson 3
.\scripts\run.ps1 -Lesson 4
.\scripts\run.ps1 -Lesson 5
.\scripts\run.ps1 -Lesson 6
.\scripts\run.ps1 -Lesson 7
```

脚本会自动选择 JDK 17（本机默认 `JAVA_HOME` 指向 JDK 8）和 Maven 3.8.4（本机默认 `mvn` 是 3.5.3，版本过低）。

### 方式 B：手动指定 JDK 和 Maven

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
D:\environment\apache-maven-3.8.4\bin\mvn.cmd spring-boot:run "-Dspring-boot.run.arguments=--demo.lesson=1"
```

### 方式 C：IDE

用 IDEA 打开 `pom.xml`，把 Project SDK 设为 17，然后在 `application.yml` 里把 `demo.lesson` 改成 `1/2/3/4/5/6/7` 后运行主类。

## 三、七个课时分别看什么

### 课时 1：基础对话

- 入口：`Lesson01BasicChat`
- 观察：控制台输出的模型回答，以及 `promptTokens / completionTokens / totalTokens`
- 关键代码：

```java
ChatResponse response = chatClient.prompt()
        .user(question)
        .call()
        .chatResponse();
```

### 课时 2：工具调用

- 入口：`Lesson02ToolCalling`
- 观察：模型是否自动调用了 `getWeather` / `isGoodForOutdoorSports`
- 关键代码：`WeatherTools` 上的 `@Tool`，请求上的 `.tools(weatherTools)`
- 注意：工具描述写得好不好，会直接影响模型是否调用、调用得对不对

### 课时 3：ReactAgent

- 入口：`Lesson03ReactAgent`
- 观察：第二轮问题“那适合去跑步吗？”没有重复城市名，但 Agent 能记住上一轮的“北京”
- 关键代码：

```java
RunnableConfig config = RunnableConfig.builder().threadId(threadId).build();
AssistantMessage first = agent.call("北京今天天气怎么样？", config);
AssistantMessage second = agent.call("那适合去跑步吗？", config);
```

### 课时 4：流式输出

- 入口：`Lesson04Streaming`（控制台）或浏览器打开 <http://localhost:8080/>
- 观察：
  - 控制台的回答是“一块一块”出现的，并统计 chunk 数量
  - 浏览器演示页可对比「流式发送」和「普通发送」：总耗时接近，但流式首字更早出现
- 关键代码：

```java
chatClient.prompt()
        .user(question)
        .stream()          // 返回 Flux<Content>
        .content()         // Flux<String>：每个元素是一个增量 chunk
        .doOnNext(System.out::print)
        .blockLast();      // 仅控制台演示用；Web 场景交给 Spring MVC 返回 SSE
```

- 原理：`ChatClient.call()` 等待完整响应；`stream()` 把模型返回的增量转成 `Flux`，Spring MVC 在 `produces = text/event-stream` 时会逐条写成 SSE 事件。
- 注意：流式提升的是**感知速度**（首字时间 TTFT），不会减少总 token 与总耗时。

### 课时 5：结构化输出

- 入口：`Lesson05StructuredOutput`
- 观察：模型先输出 JSON，再被反序列化成 `TravelPlan` 对象；控制台会打印原始 JSON、格式化对象和强类型字段
- 关键代码：

```java
TravelPlan plan = chatClient.prompt()
        .system("请严格按照要求的 JSON 结构输出，不要输出 Markdown 代码块。")
        .user(prompt)
        .call()
        .entity(TravelPlan.class);
```

- 目标类型：`model/TravelPlan.java`（record，含嵌套 `DayPlan`）
- 调试技巧：`.responseEntity(TravelPlan.class)` 可以同时拿到原始 `ChatResponse` 与解析后的对象
- 如果报 `Unexpected end-of-input: was expecting closing quote`，说明 JSON 被 `max-tokens` 截断了，把 `spring.ai.deepseek.chat.options.max-tokens` 调大（本工程已设为 2048）

### 课时 6：持久化记忆

- 入口：`Lesson06PersistentMemory`
- 观察：
  1. 第一次运行：写入“我叫小明”“问过北京天气”，然后进程停止
  2. 再次执行同样命令：日志出现 `[检测到历史记忆]`，并且能正确回答名字和城市
- 关键代码：

```java
BaseCheckpointSaver saver = FileSystemSaver.builder()
        .targetFolder(Path.of("./data/checkpoints"))
        .stateSerializer(new SpringAIStateSerializer())
        .build();

ReactAgent agent = ReactAgent.builder()
        .name("persistent_weather_agent")
        .model(chatModel)
        .methodTools(weatherTools)
        .saver(saver)
        .build();
```

- 检查点文件：`data/checkpoints/thread-<threadId>.saver`
- 清空记忆：删除 `data/checkpoints`（或对应的 `.saver` 文件）
- 生产环境：把 `FileSystemSaver` 换成 `RedisSaver` / `MysqlSaver` / `PostgresSaver`，仍是 `.saver(...)`
- 注意：记忆的隔离键始终是 `threadId`

### 课时 7：RAG 检索增强生成

- 入口：`Lesson07Rag`
- 知识库：`src/main/resources/knowledge/*.md`（项目专属资料）
- 观察：
  - 同一个问题，**不检索**时模型回答“不知道”
  - **检索后**模型能准确回答内部代号 `Aurora-7`、检查点目录 `data/checkpoints` 等知识库内容
  - 日志会打印命中的来源文件和相似度分数
- RAG 流程：
  1. 文档按二级标题切块
  2. 向量化（本地哈希 embedding，1024 维）
  3. 写入 `SimpleVectorStore`
  4. 用问题检索 topK=4
  5. 把片段拼进提示词，要求模型只依据资料回答
- 关键代码：

```java
List<Document> documents = vectorStore.similaritySearch(
        SearchRequest.builder().query(question).topK(4).build());

String answer = chatClient.prompt()
        .system("只能依据资料回答，资料没有的信息就说不知道")
        .user("【资料】\n" + buildContext(documents) + "\n\n【问题】\n" + question)
        .call()
        .content();
```

- 为什么用本地哈希模型：DeepSeek 没有 embedding 接口，本课用字符 bigram 哈希做离线演示。生产可以替换成 DashScope `text-embedding-v3`、OpenAI `text-embedding-3-small` 或本地 ONNX 模型，只需换一个 `EmbeddingModel` Bean，上层代码不变。
- 调参：`demo.rag.top-k` 控制检索片段数；知识库文件放在 `resources/knowledge/`。

## 四、Web 接口

启动后：

| 方法 | 地址 | 说明 |
| --- | --- | --- |
| GET | `/` | 课时 4 的流式输出演示页 |
| GET | `/api/chat?message=你好` | 课时 1 的普通对话 |
| GET | `/api/plan?city=杭州&days=2` | 课时 5 的结构化输出 |
| GET | `/api/rag?question=...` | 课时 7 的 RAG 问答 |
| GET | `/api/rag/search?question=...` | 只做检索，观察命中片段 |
| POST | `/api/chat/stream` | 流式输出（SSE） |
| POST | `/api/agent/chat` | 课时 3 的 ReactAgent，支持多轮 |
| POST | `/api/agent/persistent/chat` | 课时 6 的持久化记忆 Agent |

示例：

```powershell
# 普通对话
Invoke-RestMethod "http://localhost:8080/api/chat?message=你好"

# 流式输出
curl.exe -N -X POST http://localhost:8080/api/chat/stream `
  -H "Content-Type: application/json" `
  -d '{\"message\":\"用一句话介绍 Spring AI\"}'

# Agent 第一轮（返回 conversationId）
$r = Invoke-RestMethod -Method Post http://localhost:8080/api/agent/chat `
  -ContentType "application/json" `
  -Body '{"message":"北京今天天气怎么样？"}'
$r

# Agent 第二轮：带上同一个 conversationId，验证记忆
Invoke-RestMethod -Method Post http://localhost:8080/api/agent/chat `
  -ContentType "application/json" `
  -Body (@{ message = "那适合跑步吗？"; conversationId = $r.conversationId } | ConvertTo-Json)

# 结构化输出（curl 能正确显示 UTF-8 中文）
curl.exe "http://localhost:8080/api/plan?city=%E6%9D%AD%E5%B7%9E&days=2&budget=1500"

# RAG 问答（编码后的“检查点目录”）
curl.exe "http://localhost:8080/api/rag?question=%E6%A3%80%E6%9F%A5%E7%82%B9%E7%9B%AE%E5%BD%95"

# 持久化记忆：同一个 conversationId 连续两次
function Post-Json($url, $obj) {
    $json = $obj | ConvertTo-Json -Compress
    Invoke-RestMethod -Method Post $url -ContentType "application/json; charset=utf-8" `
        -Body ([Text.Encoding]::UTF8.GetBytes($json))
}
Post-Json "http://localhost:8080/api/agent/persistent/chat" @{ message = "我叫小明，请记住。"; conversationId = "demo-1" }
Post-Json "http://localhost:8080/api/agent/persistent/chat" @{ message = "我叫什么名字？"; conversationId = "demo-1" }
```

> Windows PowerShell 5.1 的 `Invoke-RestMethod` 在响应没有 charset 时会按 Latin-1 解码，中文显示为乱码，但服务端数据本身是正确的。用 `curl.exe` 或 PowerShell 7（`pwsh`）即可正常显示；发送中文请求体时建议按上面的方式转成 UTF-8 字节。

## 五、常见问题

**1. 启动时报 `InvalidApiKey` / HTTP 401**

Key 没配置或已失效。检查 `$env:DEEPSEEK_API_KEY` 是否在**同一个终端窗口**里设置，然后重启应用。

**2. `mvn` 报 `Unsupported class file major version` 或编译失败**

本机默认走 JDK 8。用 `scripts/run.ps1`，或先执行 `$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"`。

**3. 工具没有被调用**

把工具方法的 `description` 写具体（什么场景调用、参数是什么），也可以在系统提示词里明确“必须调用工具”。

**4. 重启后记忆丢失**

课时 3 用的 `MemorySaver` 是进程内存储；课时 6 的 `FileSystemSaver` 会把状态写到 `data/checkpoints`，重启后仍可恢复。生产环境再换成 Redis / MySQL / PostgreSQL。

**5. 课时 2/3 报工具相关错误**

确认 `model` 是 `deepseek-flash` 或 `deepseek-v4-pro`，不要用不支持 Function Calling 的推理模型。

**6. 流式接口看起来还是一次性返回**

- 命令行测试用 `curl.exe -N` 关闭客户端缓冲；
- 经过 Nginx 等反向代理时，需要关闭 `proxy_buffering`；
- 浏览器演示页已经按 SSE 逐事件解析，可直接对比流式与普通请求。

**7. 结构化输出解析失败**

典型报错：`Unexpected end-of-input: was expecting closing quote for a string value`。这几乎都是模型输出被截断导致的。

- 先把 `spring.ai.deepseek.chat.options.max-tokens` 调大（本工程已从 1024 调到 2048；复杂对象可用 4096）；
- 在提示词里限制输出长度，例如“每个活动不超过 20 个字”“每天 2 个活动”；
- 字段尽量用 `String / int / boolean / List` 等简单类型；
- 用 `.responseEntity(...)` 打印原始输出，先确认模型到底返回了什么。

**8. 怎么清空课时 6 的记忆**

删除 `data/checkpoints` 目录，或只删除对应的 `thread-<threadId>.saver` 文件。

**9. PowerShell 5.1 中文乱码**

这是客户端解码问题（响应无 charset 时按 Latin-1 处理），服务端数据正常。用 `curl.exe`、PowerShell 7，或读取原始 UTF-8 字节。

**10. RAG 答非所问 / 检索不到**

- 确认 `target/classes/knowledge/` 下有你的 Markdown 文件；
- 调大 `demo.rag.top-k`（例如 6），让更多片段进入上下文；
- 本地哈希 embedding 只擅长字面相似，换一种说法可能检索不到；生产请换成真正的语义 embedding；
- 切块粒度也会影响效果：单片太大容易稀释相关性，太小会丢上下文。

## 六、下一步

见 [docs/LEARNING-PATH.md](docs/LEARNING-PATH.md)。建议顺序：Graph 编排 → 多 Agent → Hooks / 可观测性。

## 参考

- 官方文档：<https://java2ai.com/docs/overview>
- Quick Start：<https://java2ai.com/docs/quick-start>
- 官方示例仓库：<https://github.com/spring-ai-alibaba/examples>
