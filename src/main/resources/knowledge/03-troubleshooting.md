# 常见问题

## 401 Authentication Fails

DeepSeek Key 未配置或无效。检查环境变量 `DEEPSEEK_API_KEY`，或 `application-local.yml` 中的 `api-key`。
Key 可以在 platform.deepseek.com 创建。

## 结构化输出被截断

报错 `Unexpected end-of-input: was expecting closing quote for a string value`，表示模型输出的 JSON 被截断。
解决办法是调大 `spring.ai.deepseek.chat.options.max-tokens`，例如 2048 或 4096，同时可以在提示词里限制输出长度。

## 流式输出变一次性

Nginx 等反向代理开启缓冲会导致 SSE 被聚合后一次性返回。
需要关闭 `proxy_buffering`；命令行测试时使用 `curl.exe -N` 关闭客户端缓冲。

## 端口被占用

8080 被占用时应用启动失败。先停止占用进程，或修改 `server.port`。

## PowerShell 中文乱码

Windows PowerShell 5.1 在响应没有 charset 时可能按 Latin-1 解码，导致中文显示乱码。
服务端数据本身正常。可以用 `curl.exe` 或 PowerShell 7（`pwsh`）查看。
