package com.example.springaialibaba.lesson;

import com.example.springaialibaba.rag.KnowledgeBase;
import com.example.springaialibaba.rag.RagAnswer;
import com.example.springaialibaba.rag.RagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 课时 7：RAG（Retrieval-Augmented Generation，检索增强生成）。
 * <p>
 * 运行：.\scripts\run.ps1 -Lesson 7
 * <p>
 * 学习要点：
 * 1. 先检索、再生成：模型只负责“读资料并总结”，事实来自知识库。
 * 2. 对照组：同一个问题，不检索时模型容易说不知道或编造。
 * 3. 检索质量取决于切块、embedding 和 topK；本课用本地哈希向量模型做离线演示。
 */
@Component
@Order(100)
@ConditionalOnProperty(prefix = "demo", name = "lesson", havingValue = "7")
public class Lesson07Rag implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(Lesson07Rag.class);

    private final RagService ragService;
    private final KnowledgeBase knowledgeBase;

    public Lesson07Rag(RagService ragService, KnowledgeBase knowledgeBase) {
        this.ragService = ragService;
        this.knowledgeBase = knowledgeBase;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("========== 课时 7：RAG 检索增强生成 ==========");
        log.info("[知识库] classpath:knowledge/*.md，共 {} 个片段", knowledgeBase.size());

        askAndCompare("本项目的内部代号是什么？课程助教叫什么名字？");
        askAndCompare("课时 6 的检查点保存在哪个目录？文件名规则是什么？怎么清空？");

        log.info("========== 课时 7 结束 ==========");
    }

    private void askAndCompare(String question) {
        log.info("");
        log.info("[问题] {}", question);

        String noRag = ragService.answerWithoutRag(question);
        log.info("[不使用 RAG] {}", noRag);

        RagAnswer ragAnswer = ragService.ask(question);
        log.info("[使用 RAG] {}", ragAnswer.answer());
        for (RagAnswer.Source source : ragAnswer.sources()) {
            log.info("   命中: {} (score={}) {}", source.source(), source.score(), source.snippet());
        }
    }
}
