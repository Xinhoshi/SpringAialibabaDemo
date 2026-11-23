# 核心概念

## ChatClient 与 ReactAgent

`ChatClient` 适合单次请求：一次调用得到一次响应，需要自己管理多轮历史。
`ReactAgent` 是一个可循环的 Agent 图，能自行完成“思考 → 调用工具 → 观察结果 → 再回答”，并内置基于 `threadId` 的状态管理。

## 两种记忆方式

`MemorySaver` 把检查点放在内存，进程重启后丢失，适合课时 3 的演示。
`FileSystemSaver` 把检查点写到磁盘，进程重启后仍可恢复，适合课时 6，也是生产环境持久化思路的简化版。

## threadId

`threadId` 是会话隔离键。相同 `threadId` 的多次调用共享历史消息，不同 `threadId` 之间互不影响。
`RunnableConfig.builder().threadId(...)` 用来指定它。

## 工具调用

普通 Java 方法加上 `@Tool` 注解即可注册为模型可调用的工具。
参数的 `@ToolParam(description = ...)` 会进入提示词，描述越清楚，模型越不容易调错。
注册方式有两种：`FunctionToolCallback`，或 ReactAgent 的 `.methodTools(bean)`。
