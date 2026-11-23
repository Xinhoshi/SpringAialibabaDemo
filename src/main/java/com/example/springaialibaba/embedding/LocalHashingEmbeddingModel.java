package com.example.springaialibaba.embedding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Component;

/**
 * 本地哈希向量模型：把文本的字符二元组（bigram）哈希到固定维度。
 * <p>
 * 为什么用它？
 * <ul>
 *   <li>本项目的对话模型是 DeepSeek，它没有 embedding 接口；</li>
 *   <li>为了让 RAG 课时不依赖额外的云服务 / Key / 模型下载，先用一个可离线运行的替代实现；</li>
 *   <li>它能捕捉字面相似度，足以演示“文档切分 → 向量化 → 检索 → 拼上下文 → 生成”的完整链路。</li>
 * </ul>
 * <p>
 * 注意：它不是语义向量模型，同义词（如“检查点”与“快照”）检索效果有限。
 * 生产环境请替换为 DashScope text-embedding-v3、OpenAI text-embedding-3-small
 * 或本地 ONNX 模型，只需提供另一个 {@link EmbeddingModel} Bean，上层代码无需改动。
 */
@Component
public class LocalHashingEmbeddingModel implements EmbeddingModel {

    private static final int DIMENSIONS = 1024;

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<Embedding> embeddings = new ArrayList<>();
        List<String> instructions = request.getInstructions();
        for (int i = 0; i < instructions.size(); i++) {
            embeddings.add(new Embedding(vectorize(instructions.get(i)), i));
        }
        return new EmbeddingResponse(embeddings);
    }

    @Override
    public float[] embed(Document document) {
        return vectorize(document.getText());
    }

    @Override
    public int dimensions() {
        return DIMENSIONS;
    }

    private float[] vectorize(String text) {
        float[] vector = new float[DIMENSIONS];
        if (text == null || text.isBlank()) {
            return vector;
        }

        // 去掉空白，中文按字符 bigram，英文也按字符 bigram
        String normalized = text.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
        for (int i = 0; i + 1 < normalized.length(); i++) {
            String bigram = normalized.substring(i, i + 2);
            int index = Math.floorMod(bigram.hashCode(), DIMENSIONS);
            vector[index] += 1.0f;
        }

        // L2 归一化，配合余弦相似度
        double norm = 0.0;
        for (float value : vector) {
            norm += value * value;
        }
        norm = Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] = (float) (vector[i] / norm);
            }
        }
        return vector;
    }
}
