package com.example.springaialibaba.web.dto;

/**
 * 一次对话请求。
 *
 * @param message        用户问题
 * @param conversationId 会话 ID；Agent 接口用它作为 threadId，为空则自动生成
 */
public record ChatRequest(String message, String conversationId) {
}
