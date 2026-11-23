package com.example.springaialibaba.rag;

import java.util.List;

/**
 * RAG 回答结果：答案 + 命中的知识片段（来源、相似度、摘要）。
 */
public record RagAnswer(String answer, List<Source> sources) {

    public record Source(String source, Double score, String snippet) {
    }
}
