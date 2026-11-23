# 学习路线

> 目标：从“会调用大模型”到“能编排多 Agent 应用”。
> 已完成课时标 ✅，后续课时按需添加，保持一节课一个可运行入口。

## 阶段一：模型调用基础

| 课时 | 主题 | 关键 API / 概念 | 状态 |
| --- | --- | --- | --- |
| 1 | 基础对话 | `ChatClient`、`ChatResponse`、token 用量 | ✅ |
| 2 | 工具调用 | `@Tool`、`@ToolParam`、`.tools(...)` | ✅ |
| 3 | ReactAgent 入门 | `ReactAgent`、`RunnableConfig.threadId`、`MemorySaver` | ✅ |
| 4 | 流式输出 | `.stream().content()`、SSE、`Flux` | ✅ |
| 5 | 提示词工程 | 系统提示词、`PromptTemplate`、模板变量 | 待做 |
| 6 | 结构化输出 | `.entity(Class)`、`outputType`、`outputSchema` | ✅ |

## 阶段二：记忆与知识

| 课时 | 主题 | 关键 API / 概念 | 状态 |
| --- | --- | --- | --- |
| 7 | 多轮记忆 | `ChatMemory`、`MessageChatMemoryAdvisor` | 待做 |
| 8 | 持久化记忆 | `BaseCheckpointSaver`、`FileSystemSaver`、Redis / MySQL Saver | ✅ |
| 9 | RAG 入门 | `EmbeddingModel`、`VectorStore`、`QuestionAnswerAdvisor` | 待做 |
| 10 | 文档入库 | `DocumentReader`、切分、向量化 | 待做 |

## 阶段三：Agent 与编排

| 课时 | 主题 | 关键 API / 概念 | 状态 |
| --- | --- | --- | --- |
| 11 | Hooks | `ModelCallLimitHook`、`HumanInTheLoopHook` | 待做 |
| 12 | 子 Agent | 多 Agent 协作、`asNode()` | 待做 |
| 13 | Graph 编排 | `StateGraph`、节点、边、条件路由 | 待做 |
| 14 | 可观测性 | Observation、Studio、日志与追踪 | 待做 |

## 每节课的固定套路

1. 在 `lesson` 包下新增 `LessonXX...`，用 `@ConditionalOnProperty(prefix="demo", name="lesson", havingValue="XX")` 控制启动。
2. 在 `config` 中准备需要的 Bean（模型、工具、Advisor、Saver 等）。
3. 在 `README.md` 的课时表补一行，写清楚“看什么现象、关键 API 是什么”。
4. 能做成 HTTP 接口的，同步加到 `ChatController`。

## 推荐阅读顺序（官方文档）

1. 概览：<https://java2ai.com/docs/overview>
2. Quick Start：<https://java2ai.com/docs/quick-start>
3. Agent Framework 教程：<https://java2ai.com/docs/frameworks/agent-framework/tutorials/agents>
4. 记忆管理：<https://java2ai.com/docs/frameworks/agent-framework/tutorials/memory>
5. Graph Core：<https://java2ai.com/docs/frameworks/graph-core/quick-start>
6. 生态集成（ChatClient / ChatModel / RAG 等）：<https://java2ai.com/integration/chatclient>
7. 官方示例：<https://github.com/spring-ai-alibaba/examples>

## 学习建议

- **先现象，后原理**：每节课先跑起来，看到输入输出，再回头读源码和文档。
- **一次只改一个变量**：课时 2 先只加一个工具，确认被调用后再加第二个。
- **提示词和工具描述是“代码”**：改一个字都可能影响模型行为，值得反复试验。
- **记忆要分清两层**：`ChatClient` 的 `ChatMemory` 与 Agent 的 `threadId + CheckpointSaver` 不是一回事。
- **不要过早引入向量库**：先用模拟数据和单轮 Agent 把链路跑通，再上 RAG。
