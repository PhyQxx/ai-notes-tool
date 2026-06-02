package com.ainotes.config;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.redis.RedisEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class RAGConfig {

    @Value("${ai.rag.enabled:false}")
    private boolean ragEnabled;

    @Value("${ai.deepseek.api-key}")
    private String deepseekApiKey;

    @Value("${ai.deepseek.base-url}")
    private String deepseekBaseUrl;

    @Value("${spring.data.redis.host:127.0.0.1}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        if (!ragEnabled || deepseekApiKey == null || deepseekApiKey.isEmpty()) {
            log.warn("DeepSeek API Key 未配置或 RAG 已关闭，使用 DummyChatLanguageModel.");
            return (messages) -> Response.from(AiMessage.from("RAG 功能未启用。"));
        }
        return OpenAiChatModel.builder()
                .apiKey(deepseekApiKey)
                .baseUrl(deepseekBaseUrl)
                .modelName("deepseek-chat")
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        if (!ragEnabled) {
            log.info("向量搜索已通过配置 (ai.rag.enabled=false) 关闭，使用 DummyEmbeddingModel.");
            return textSegments -> Response.from(textSegments.stream()
                    .map(s -> new Embedding(new float[384]))
                    .collect(Collectors.toList()));
        }
        return new AllMiniLmL6V2QuantizedEmbeddingModel();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        if (!ragEnabled) {
            return new InMemoryEmbeddingStore<TextSegment>() {
                @Override
                public String add(Embedding embedding, TextSegment textSegment) { return ""; }
                @Override
                public List<String> addAll(List<Embedding> embeddings, List<TextSegment> embedded) { return Collections.emptyList(); }
                @Override
                public List<EmbeddingMatch<TextSegment>> findRelevant(Embedding referenceEmbedding, int maxResults, double minScore) { return Collections.emptyList(); }
            };
        }
        try {
            log.info("正在尝试连接 Redis Stack (向量搜索)... {}:{}", redisHost, redisPort);
            return RedisEmbeddingStore.builder()
                    .host(redisHost)
                    .port(redisPort)
                    .dimension(384) // AllMiniLmL6V2 dimension
                    .indexName("ainotes:embeddings")
                    .build();
        } catch (Exception e) {
            log.error("Redis Stack 连接失败或不支持 RediSearch (FT._LIST): {}. 切换到内存模式 (开发环境).", e.getMessage());
            log.warn("注意: 内存模式下的向量索引不会持久化, 重启后 RAG 功能将重新索引.");
            return new InMemoryEmbeddingStore<>();
        }
    }

    @Bean
    public ContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        if (!ragEnabled) {
            return query -> Collections.emptyList();
        }
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .minScore(0.6)
                .build();
    }

    @Bean
    public KnowledgeAssistant knowledgeAssistant(ChatLanguageModel chatLanguageModel, ContentRetriever contentRetriever) {
        if (!ragEnabled) {
            return new KnowledgeAssistant() {
                @Override
                public String answer(String query) {
                    return "向量搜索和知识库功能当前已在配置文件中关闭 (ai.rag.enabled=false)。";
                }
                @Override
                public TokenStream answerStream(String query) {
                    throw new UnsupportedOperationException("Streaming RAG is disabled.");
                }
            };
        }
        return AiServices.builder(KnowledgeAssistant.class)
                .chatLanguageModel(chatLanguageModel)
                .contentRetriever(contentRetriever)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }
}
