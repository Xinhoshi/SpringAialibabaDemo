package com.example.springaialibaba.web.dto;

/**
 * 一次对话响应。
 *
 * @param answer         模型回答
 * @param conversationId 会话 ID；后续请求带上它即可延续同一段对话
 */
public record ChatReply(String answer, String conversationId) {
}
