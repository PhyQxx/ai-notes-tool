package com.ainotes.service;

import com.ainotes.entity.Note;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import com.ainotes.dto.response.NoteClusterResponse;
import com.ainotes.ai.AIProvider;
import com.ainotes.ai.AIProviderFactory;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    
    @org.springframework.context.annotation.Lazy
    @org.springframework.beans.factory.annotation.Autowired
    private NoteService noteService;
    
    private final AIProviderFactory aiProviderFactory;

    /**
     * 对用户的笔记进行语义聚类
     */
    public List<NoteClusterResponse> clusterNotes(Long userId, int k) {
        List<Note> allNotes = noteService.listNotes(userId, null).getRecords();
        if (allNotes.isEmpty()) return new ArrayList<>();
        
        if (allNotes.size() < k) {
            k = Math.max(1, allNotes.size() / 2);
        }

        // 1. 获取所有笔记的 Embedding
        List<DoublePoint> points = new ArrayList<>();
        List<Note> validNotes = new ArrayList<>();

        for (Note note : allNotes) {
            if (note.getContent() != null && !note.getContent().isBlank()) {
                float[] vector = embeddingModel.embed(note.getTitle() + "\n" + note.getContent()).content().vector();
                double[] dVector = new double[vector.length];
                for (int i = 0; i < vector.length; i++) dVector[i] = vector[i];
                points.add(new DoublePoint(dVector));
                validNotes.add(note);
            }
        }

        if (points.isEmpty()) return new ArrayList<>();

        // 2. K-means 聚类
        KMeansPlusPlusClusterer<DoublePoint> clusterer = new KMeansPlusPlusClusterer<>(k, 100);
        List<CentroidCluster<DoublePoint>> clusters = clusterer.cluster(points);

        List<NoteClusterResponse> responses = new ArrayList<>();
        AIProvider aiProvider = aiProviderFactory.getProvider("deepseek");

        // 3. 为每个簇生成名称和关键字
        for (int i = 0; i < clusters.size(); i++) {
            CentroidCluster<DoublePoint> cluster = clusters.get(i);
            List<Long> clusterNoteIds = new ArrayList<>();
            List<String> clusterNoteTitles = new ArrayList<>();

            for (DoublePoint point : cluster.getPoints()) {
                int index = points.indexOf(point);
                Note note = validNotes.get(index);
                clusterNoteIds.add(note.getId());
                clusterNoteTitles.add(note.getTitle());
            }

            if (clusterNoteIds.isEmpty()) continue;

            // 调用 AI 生成聚类名称
            String titles = String.join(", ", clusterNoteTitles);
            String prompt = String.format("以下是一组笔记的标题: [%s]。请为这组笔记生成一个简短的分类名称（5个字以内）和3个相关的关键字（以逗号分隔）。输出格式: 名称 | 关键字1, 关键字2, 关键字3", titles);
            
            try {
                String aiResponse = aiProvider.chat(aiProvider.getDefaultModel(), List.of(Map.of("role", "user", "content", prompt)));
                String[] parts = aiResponse.split("\\|");
                String name = parts.length > 0 ? parts[0].trim() : "未命名分类 " + (i + 1);
                String[] keywords = parts.length > 1 ? parts[1].split(",") : new String[]{"知识", "笔记", "关联"};
                
                responses.add(new NoteClusterResponse(
                    name,
                    clusterNoteIds,
                    java.util.Arrays.stream(keywords).map(String::trim).collect(Collectors.toList())
                ));
            } catch (Exception e) {
                log.error("AI 聚类名称生成失败", e);
                responses.add(new NoteClusterResponse("分类 " + (i + 1), clusterNoteIds, List.of("知识", "自动聚类")));
            }
        }

        return responses;
    }

    @PostConstruct
    public void init() {
        log.info("开始初始化 AI 知识库索引...");
        reindexAll();
    }

    /**
     * 全量重新索引
     */
    public void reindexAll() {
        List<Note> allNotes = noteService.listNotes(null, null).getRecords();
        log.info("发现 {} 篇笔记，开始索引...", allNotes.size());
        allNotes.forEach(this::indexNote);
        log.info("知识库索引初始化完成");
    }

    /**
     * 索引单篇笔记
     */
    @Async
    public void indexNote(Note note) {
        if (note.getContent() == null || note.getContent().isBlank()) {
            return;
        }

        Metadata metadata = Metadata.from("noteId", note.getId().toString());
        if (note.getFolderId() != null) metadata.add("folderId", note.getFolderId().toString());
        if (note.getSpaceId() != null) metadata.add("spaceId", note.getSpaceId().toString());
        if (note.getTags() != null) metadata.add("tags", note.getTags());

        Document document = Document.from(
                note.getTitle() + "\n\n" + note.getContent(),
                metadata
        );

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .build();

        ingestor.ingest(document);
        log.debug("笔记 {} 已加入知识库索引 (含元数据)", note.getId());
    }

    /**
     * 寻找相似笔记
     */
    public List<Note> findSimilarNotes(Long userId, Long noteId, int limit, Map<String, String> filterMetadata) {
        Note note = noteService.getNoteDetail(userId, noteId);
        if (note == null || note.getContent() == null || note.getContent().isBlank()) {
            return new ArrayList<>();
        }

        String text = note.getTitle() + "\n" + note.getContent();
        dev.langchain4j.data.embedding.Embedding queryEmbedding = embeddingModel.embed(text).content();
        
        // LangChain4j 0.29.0 findRelevant API
        List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.findRelevant(queryEmbedding, limit + 1);
        
        return relevant.stream()
                .map(match -> {
                    String idStr = match.embedded().metadata().getString("noteId");
                    if (idStr == null) return null;
                    Long matchedId = Long.parseLong(idStr);
                    if (matchedId.equals(noteId)) return null;
                    return noteService.getNoteDetail(userId, matchedId);
                })
                .filter(java.util.Objects::nonNull)
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * AI 知识回顾
     */
    public List<Note> getFlashbackNotes(Long userId, int limit) {
        List<Note> recent = noteService.recentNotes(userId, 1);
        if (recent.isEmpty()) return new ArrayList<>();
        Note anchor = recent.get(0);

        List<Note> similar = findSimilarNotes(userId, anchor.getId(), 10, Map.of());

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return similar.stream()
                .filter(n -> n.getUpdatedAt().isBefore(thirtyDaysAgo))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
