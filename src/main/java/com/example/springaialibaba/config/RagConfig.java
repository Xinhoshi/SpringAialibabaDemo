package com.example.springaialibaba.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RAG 用的内存向量库。
 * <p>
 * 想换成生产向量库（PGVector / Milvus / Redis 等）时，把这里的 Bean 换掉即可，
 * KnowledgeBase 和 RagService 只依赖 VectorStore 接口。
 */
@Configuration
public class RagConfig {

    @Bean
    public SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
