package com.example.springaialibaba.rag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * RAG 主流程：
 * 1. 用问题去向量库检索最相关的知识片段；
 * 2. 把片段拼进提示词；
 * 3. 要求模型只依据资料回答，并返回命中的来源。
 */
@Service
public class RagService {

    private final ChatClient chatClient;
    private final KnowledgeBase knowledgeBase;
    private final int topK;

    public RagService(ChatClient chatClient,
            KnowledgeBase knowledgeBase,
            @Value("${demo.rag.top-k:4}") int topK) {
        this.chatClient = chatClient;
        this.knowledgeBase = knowledgeBase;
        this.topK = topK;
    }

    public RagAnswer ask(String question) {
        List<Document> documents = knowledgeBase.search(question, topK);

        String answer = chatClient.prompt()
                .system("""
                        你是严谨的学习助手。只能依据用户提供的【资料】回答问题。
                        资料中没有的信息，请回答“资料中没有提到”，不要编造。
                        回答尽量简短，不要输出与问题无关的内容。
                        """)
                .user("【资料】\n" + buildContext(documents) + "\n\n【问题】\n" + question)
                .call()
                .content();

        return new RagAnswer(answer, toSources(documents));
    }

    /**
     * 对照组：不检索资料，直接问模型，用于观察幻觉。
     */
    public String answerWithoutRag(String question) {
        return chatClient.prompt()
                .system("你是一个学习助手。不知道就明确回答“不知道”，不要编造。")
                .user(question)
                .call()
                .content();
    }

    public List<RagAnswer.Source> search(String question, int limit) {
        return toSources(knowledgeBase.search(question, limit));
    }

    private String buildContext(List<Document> documents) {
        if (documents.isEmpty()) {
            return "（没有检索到相关资料）";
        }

        StringBuilder context = new StringBuilder();
        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            context.append("[").append(i + 1).append("] 来源：")
                    .append(Objects.toString(document.getMetadata().get("source"), "unknown"))
                    .append("\n")
                    .append(document.getText())
                    .append("\n\n");
        }
        return context.toString();
    }

    private List<RagAnswer.Source> toSources(List<Document> documents) {
        List<RagAnswer.Source> sources = new ArrayList<>();
        for (Document document : documents) {
            String text = document.getText() == null ? "" : document.getText();
            String snippet = text.length() > 120 ? text.substring(0, 120) + "..." : text;
            sources.add(new RagAnswer.Source(
                    Objects.toString(document.getMetadata().get("source"), "unknown"),
                    document.getScore(),
                    snippet.replaceAll("\\s+", " ")));
        }
        return sources;
    }
}
