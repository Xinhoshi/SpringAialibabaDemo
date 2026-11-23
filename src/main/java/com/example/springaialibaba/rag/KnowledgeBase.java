package com.example.springaialibaba.rag;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * 知识库：加载 classpath:knowledge/*.md，按二级标题切块，写入向量库。
 */
@Component
public class KnowledgeBase {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBase.class);

    private static final int MAX_CHUNK_LENGTH = 800;

    private final VectorStore vectorStore;
    private final List<Document> chunks = new ArrayList<>();

    public KnowledgeBase(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        load();
    }

    public List<Document> search(String question, int topK) {
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(topK)
                        .build());
    }

    public int size() {
        return chunks.size();
    }

    private void load() {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath*:knowledge/*.md");
            Arrays.sort(resources, Comparator.comparing(r -> String.valueOf(r.getFilename())));

            List<Document> documents = new ArrayList<>();
            for (Resource resource : resources) {
                String content = resource.getContentAsString(StandardCharsets.UTF_8);
                String source = resource.getFilename();
                for (String chunkText : splitMarkdown(content)) {
                    documents.add(Document.builder()
                            .text(chunkText)
                            .metadata("source", source)
                            .build());
                }
            }

            if (documents.isEmpty()) {
                log.warn("知识库为空：没有找到 classpath:knowledge/*.md");
                return;
            }

            vectorStore.add(documents);
            chunks.addAll(documents);
            log.info("知识库加载完成：{} 个文件，{} 个片段", resources.length, documents.size());
        }
        catch (IOException e) {
            throw new IllegalStateException("加载知识库失败", e);
        }
    }

    /**
     * 按 Markdown 二级标题切块；过短的块（例如只有一级标题）合并到下一块，
     * 过长的块再按空行切分，避免单块太大。
     */
    private List<String> splitMarkdown(String content) {
        List<String> result = new ArrayList<>();
        String[] sections = content.split("(?m)^## ");

        StringBuilder pending = new StringBuilder();
        for (String section : sections) {
            String text = section.trim();
            if (text.isEmpty()) {
                continue;
            }
            if (text.length() < 40) {
                pending.append(text).append("\n\n");
                continue;
            }

            String merged = (pending + text).trim();
            pending.setLength(0);
            appendChunk(result, merged);
        }

        if (pending.length() > 0) {
            if (result.isEmpty()) {
                result.add(pending.toString().trim());
            }
            else {
                int last = result.size() - 1;
                result.set(last, result.get(last) + "\n\n" + pending.toString().trim());
            }
        }
        return result;
    }

    private void appendChunk(List<String> result, String text) {
        if (text.length() <= MAX_CHUNK_LENGTH) {
            result.add(text);
            return;
        }

        StringBuilder current = new StringBuilder();
        for (String paragraph : text.split("\\n\\s*\\n")) {
            if (current.length() + paragraph.length() > MAX_CHUNK_LENGTH && current.length() > 0) {
                result.add(current.toString().trim());
                current.setLength(0);
            }
            current.append(paragraph).append("\n\n");
        }
        if (current.length() > 0) {
            result.add(current.toString().trim());
        }
    }
}
