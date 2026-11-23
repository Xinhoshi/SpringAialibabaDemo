# Spring AI Alibaba 学习工程概览

## 项目定位

这是一个按课时递进的 Spring AI Alibaba 学习工程，包名 `com.example.springaialibaba`，Web 服务默认端口 8080。

## 技术栈

JDK 17、Spring Boot 3.5.10、Spring AI 1.1.2、Spring AI Alibaba 1.1.2.3。
对话模型使用 DeepSeek 的 `deepseek-flash`，通过 `spring-ai-starter-model-deepseek` 接入。
默认 `max-tokens` 为 2048。

## 内部代号

本学习工程的内部代号是 **Aurora-7**，课程助教的名字叫 **小林**。
这条信息只出现在项目知识库中，用于验证 RAG 是否真的检索到了资料。

## 课时列表

- 课时 1：基础对话，使用 `ChatClient.call()`
- 课时 2：工具调用，使用 `@Tool` 与 `.tools(...)`
- 课时 3：ReactAgent 与 `MemorySaver`
- 课时 4：流式输出 SSE
- 课时 5：结构化输出 `.entity(Class)`
- 课时 6：持久化记忆 `FileSystemSaver`
- 课时 7：RAG 检索增强生成

## 接口清单

- `GET /api/chat`
- `POST /api/chat/stream`
- `POST /api/agent/chat`
- `POST /api/agent/persistent/chat`
- `GET /api/plan`
- `GET /api/rag`

## 检查点

课时 6 使用 `FileSystemSaver`，检查点保存在 `data/checkpoints` 目录。
文件名规则是 `thread-<threadId>.saver`，例如 `thread-lesson-06-persistent.saver`。
删除 `data/checkpoints` 目录即可清空全部持久化记忆。
