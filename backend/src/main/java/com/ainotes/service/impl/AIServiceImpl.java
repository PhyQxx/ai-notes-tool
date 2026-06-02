package com.ainotes.service.impl;

import com.ainotes.ai.AIProvider;
import com.ainotes.ai.AIProviderFactory;
import com.ainotes.ai.DeepSeekProvider;
import com.ainotes.ai.GLMProvider;
import com.ainotes.ai.MiniMaxProvider;
import com.ainotes.common.exception.BusinessException;
import com.ainotes.config.AIConfig;
import com.ainotes.dto.request.AIChatRequest;
import com.ainotes.dto.request.AIConfigUpdateRequest;
import com.ainotes.dto.request.AIGenerateRequest;
import com.ainotes.dto.response.AIChatResponse;
import com.ainotes.dto.response.AIConfigResponse;
import com.ainotes.dto.response.AIConversationMessagesResponse;
import com.ainotes.entity.AIChatMessage;
import com.ainotes.entity.AIConversation;
import com.ainotes.entity.Note;
import com.ainotes.entity.UserAiConfig;
import com.ainotes.mapper.AIConversationMapper;
import com.ainotes.mapper.UserAiConfigMapper;
import com.ainotes.service.AIChatMessageService;
import com.ainotes.service.AIService;
import com.ainotes.interceptor.PromptInjectionInterceptor;
import com.ainotes.service.NoteService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI服务实现类
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl extends ServiceImpl<AIConversationMapper, AIConversation> implements AIService {

    private final AIProviderFactory providerFactory;
    private final NoteService noteService;
    private final UserAiConfigMapper userAiConfigMapper;
    private final AIChatMessageService chatMessageService;
    private final AIConfig aiConfig;
    private final com.ainotes.service.KnowledgeBaseService knowledgeBaseService;
    private static final int MAX_CONTEXT_ROUNDS = 20;
    private static final String SYSTEM_PROMPT = "你是一个智能笔记助手，可以帮助用户整理笔记、回答问题、生成内容。请用简洁、准确的方式回答。";

    @Override
    public AIChatResponse chat(Long userId, AIChatRequest request) {
        if (PromptInjectionInterceptor.containsInjection(request.getMessage())) {
            throw new BusinessException("消息包含不安全内容，请修改后重试");
        }
        AIProvider provider = getProviderWithApiKey(userId, request.getProvider());
        AIConversation conversation = getOrCreateConversation(userId, request);

        // Track if this is a new conversation (no prior messages) so we can set a proper title
        boolean isNewConversation = conversation.getMessageList() == null || conversation.getMessageList().isEmpty();

        // Save user message
        chatMessageService.saveMessage(conversation.getId(), "user", request.getMessage());

        // Build messages with context
        List<Map<String, String>> messages = buildContextMessages(conversation.getId(), request.getMessage());

        String aiResponse = provider.chat(request.getModel(), messages);
        log.info("AI对话完成, userId={}, provider={}, model={}, tokens={}", userId, request.getProvider(), request.getModel(), aiResponse.length()); // 粗略估算

        // Save assistant message
        chatMessageService.saveMessage(conversation.getId(), "assistant", aiResponse);

        // Set conversation title for new conversations
        if (isNewConversation) {
            String title = generateConversationTitle(request.getMessage());
            conversation.setTitle(title);
            updateById(conversation);
        }

        AIChatResponse response = new AIChatResponse();
        response.setConversationId(conversation.getId());
        response.setMessage(aiResponse);
        response.setRole("assistant");
        response.setTitle(conversation.getTitle());

        return response;
    }

    @Override
    public SseEmitter chatStream(Long userId, AIChatRequest request) {
        if (PromptInjectionInterceptor.containsInjection(request.getMessage())) {
            SseEmitter errEmitter = new SseEmitter();
            try { errEmitter.send(SseEmitter.event().data("{\"error\":\"消息包含不安全内容\"}")); } catch (Exception ignored) {}
            errEmitter.complete();
            return errEmitter;
        }
        SseEmitter emitter = new SseEmitter(300_000L);
        AIProvider provider = getProviderWithApiKey(userId, request.getProvider());
        AIConversation conversation = getOrCreateConversation(userId, request);

        // Save user message immediately
        chatMessageService.saveMessage(conversation.getId(), "user", request.getMessage());

        List<Map<String, String>> messages = buildContextMessages(conversation.getId(), request.getMessage());

        StringBuilder fullResponse = new StringBuilder();
        Long convId = conversation.getId();

        emitter.onCompletion(() -> {});
        emitter.onTimeout(emitter::complete);
        emitter.onError(e -> emitter.complete());

        provider.chatStream(request.getModel(), messages,
                token -> {
                    fullResponse.append(token);
                    try {
                        emitter.send(SseEmitter.event().data("{\"content\":\"" + token.replace("\"", "\\\"") + "\"}", org.springframework.http.MediaType.APPLICATION_JSON));
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                },
                () -> {
                    chatMessageService.saveMessage(convId, "assistant", fullResponse.toString());
                    updateById(conversation);
                    try {
                        emitter.send(SseEmitter.event().data("{\"conversationId\":" + convId + ",\"done\":true}", org.springframework.http.MediaType.APPLICATION_JSON));
                        emitter.complete();
                    } catch (Exception ignored) {}
                }
        );

        return emitter;
    }

    @Override
    public String generate(Long userId, AIGenerateRequest request) {
        // 获取笔记信息
        Note note = noteService.getNoteDetail(userId, request.getNoteId());
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }

        // 获取AI Provider
        AIProvider provider = getProviderWithApiKey(userId, request.getProvider());

        // 构建提示词
        String prompt = buildPromptForGenerate(request.getType(), note, request.getPrompt());

        // 构建消息列表
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", "你是一个专业的笔记助手，帮助用户优化和生成笔记内容。"));
        messages.add(Map.of("role", "user", "content", prompt));

        // 调用AI服务
        String aiResponse = provider.chat(request.getModel(), messages);

        return aiResponse;
    }

    @Override
    public IPage<AIConversation> getConversations(Long userId, Integer page, Integer pageSize) {
        Page<AIConversation> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<AIConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AIConversation::getUserId, userId);
        wrapper.orderByDesc(AIConversation::getUpdatedAt);
        return page(pageParam, wrapper);
    }

    @Override
    public AIConversation getConversationDetail(Long userId, Long conversationId) {
        AIConversation conversation = getById(conversationId);
        if (conversation == null) {
            throw new BusinessException("对话不存在");
        }
        if (!conversation.getUserId().equals(userId)) {
            throw new BusinessException("无权访问此对话");
        }
        return conversation;
    }

    @Override
    public void deleteConversation(Long userId, Long conversationId) {
        AIConversation conversation = getById(conversationId);
        if (conversation == null) {
            throw new BusinessException("对话不存在");
        }
        if (!conversation.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此对话");
        }
        removeById(conversationId);
        chatMessageService.deleteByConversationId(conversationId);
    }

    @Override
    public void renameConversation(Long userId, Long conversationId, String title) {
        AIConversation conversation = getById(conversationId);
        if (conversation == null) {
            throw new BusinessException("对话不存在");
        }
        if (!conversation.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此对话");
        }
        conversation.setTitle(title);
        updateById(conversation);
    }

    @Override
    public AIConversation createConversation(Long userId, Long noteId, String provider, String model) {
        AIConversation conversation = new AIConversation();
        conversation.setUserId(userId);
        conversation.setNoteId(noteId);
        conversation.setAiProvider(provider);
        conversation.setAiModel(model);
        conversation.setMessageList(new ArrayList<>());
        conversation.setTitle("新对话");
        save(conversation);
        return conversation;
    }

    @Override
    public AIConfigResponse getConfig(Long userId) {
        // 从数据库获取用户配置
        UserAiConfig dbConfig = userAiConfigMapper.selectOne(
                new LambdaQueryWrapper<UserAiConfig>().eq(UserAiConfig::getUserId, userId));

        String provider = dbConfig != null && dbConfig.getProvider() != null ? dbConfig.getProvider() : "deepseek";
        String model = dbConfig != null && dbConfig.getModel() != null ? dbConfig.getModel() : "deepseek-chat";

        // Check if user has their own API key configured in DB
        boolean hasUserKey = dbConfig != null && (
            ("deepseek".equalsIgnoreCase(provider) && dbConfig.getDeepseekApiKey() != null && !dbConfig.getDeepseekApiKey().isEmpty()) ||
            ("glm".equalsIgnoreCase(provider) && dbConfig.getGlmApiKey() != null && !dbConfig.getGlmApiKey().isEmpty()) ||
            ("minimax".equalsIgnoreCase(provider) && dbConfig.getMinimaxApiKey() != null && !dbConfig.getMinimaxApiKey().isEmpty())
        );

        // Check global fallback key (from application.yml/env)
        boolean hasGlobalKey = false;
        if ("deepseek".equalsIgnoreCase(provider)) {
            String globalKey = aiConfig.getDeepseek().getApiKey();
            hasGlobalKey = globalKey != null && !globalKey.isEmpty();
        } else if ("glm".equalsIgnoreCase(provider)) {
            String globalKey = aiConfig.getGlm().getApiKey();
            hasGlobalKey = globalKey != null && !globalKey.isEmpty();
        } else if ("minimax".equalsIgnoreCase(provider)) {
            String globalKey = aiConfig.getMinimax().getApiKey();
            hasGlobalKey = globalKey != null && !globalKey.isEmpty();
        }

        boolean hasApiKey = hasUserKey || hasGlobalKey;

        // 获取提供商信息
        AIProvider currentProvider = providerFactory.getProvider(provider);
        List<AIConfigResponse.ProviderInfo> providers = providerFactory.getProviderInfos();

        return AIConfigResponse.builder()
                .provider(provider)
                .model(model)
                .providers(providers)
                .models(currentProvider.getSupportedModels())
                .hasApiKey(hasApiKey)
                .build();
    }

    @Override
    @Transactional
    public void updateConfig(Long userId, AIConfigUpdateRequest request) {
        // 验证提供商和模型
        AIProvider provider = providerFactory.getProvider(request.getProvider());
        if (!provider.getSupportedModels().contains(request.getModel())) {
            throw new BusinessException("不支持的模型: " + request.getModel());
        }

        // 查询是否已有配置
        UserAiConfig existing = userAiConfigMapper.selectOne(
                new LambdaQueryWrapper<UserAiConfig>().eq(UserAiConfig::getUserId, userId));

        if (existing != null) {
            // 更新已有配置
            existing.setProvider(request.getProvider());
            existing.setModel(request.getModel());
            existing.setDeepseekApiKey(request.getDeepseekApiKey());
            existing.setGlmApiKey(request.getGlmApiKey());
            existing.setMinimaxApiKey(request.getMinimaxApiKey());
            userAiConfigMapper.updateById(existing);
        } else {
            // 新建配置
            UserAiConfig newConfig = new UserAiConfig();
            newConfig.setUserId(userId);
            newConfig.setProvider(request.getProvider());
            newConfig.setModel(request.getModel());
            newConfig.setDeepseekApiKey(request.getDeepseekApiKey());
            newConfig.setGlmApiKey(request.getGlmApiKey());
            newConfig.setMinimaxApiKey(request.getMinimaxApiKey());
            userAiConfigMapper.insert(newConfig);
        }

        log.info("用户 {} 更新AI配置: provider={}, model={}", userId, request.getProvider(), request.getModel());
    }

    @Override
    public List<AIConfigResponse.ProviderInfo> getProviders() {
        return providerFactory.getProviderInfos();
    }

    /**
     * 获取AI Provider（带API Key）
     * 优先级：用户数据库配置 > 全局环境变量
     */
    private AIProvider getProviderWithApiKey(Long userId, String providerName) {
        // 从数据库获取用户配置
        UserAiConfig dbConfig = userAiConfigMapper.selectOne(
                new LambdaQueryWrapper<UserAiConfig>().eq(UserAiConfig::getUserId, userId));

        // 默认使用 deepseek
        if (providerName == null || providerName.isEmpty()) {
            if (dbConfig != null && dbConfig.getProvider() != null) {
                providerName = dbConfig.getProvider();
            } else {
                providerName = "deepseek";
            }
        }

        String apiKey = null;
        if (dbConfig != null) {
            if ("deepseek".equalsIgnoreCase(providerName)) {
                apiKey = dbConfig.getDeepseekApiKey();
            } else if ("glm".equalsIgnoreCase(providerName)) {
                apiKey = dbConfig.getGlmApiKey();
            } else if ("minimax".equalsIgnoreCase(providerName)) {
                apiKey = dbConfig.getMinimaxApiKey();
            }
        }

        // Fallback to global API key if user hasn't configured their own
        if ((apiKey == null || apiKey.isEmpty())) {
            if ("deepseek".equalsIgnoreCase(providerName)) {
                apiKey = aiConfig.getDeepseek().getApiKey();
            } else if ("glm".equalsIgnoreCase(providerName)) {
                apiKey = aiConfig.getGlm().getApiKey();
            } else if ("minimax".equalsIgnoreCase(providerName)) {
                apiKey = aiConfig.getMinimax().getApiKey();
            }
        }

        // Check if we have a valid API key
        if (apiKey == null || apiKey.isEmpty()) {
            throw new BusinessException("请先在设置页配置 DeepSeek API Key，访问 https://platform.deepseek.com 申请");
        }

        return providerFactory.getProvider(providerName, apiKey);
    }

    /**
     * 获取或创建对话
     */
    private AIConversation getOrCreateConversation(Long userId, AIChatRequest request) {
        if (request.getConversationId() != null) {
            AIConversation conversation = getById(request.getConversationId());
            if (conversation != null && conversation.getUserId().equals(userId)) {
                return conversation;
            }
        }

        // 创建新对话
        AIConversation conversation = new AIConversation();
        conversation.setUserId(userId);
        conversation.setNoteId(request.getNoteId());
        conversation.setAiProvider(request.getProvider());
        conversation.setAiModel(request.getModel());
        conversation.setMessageList(new ArrayList<>());
        save(conversation);

        return conversation;
    }

    /**
     * 构建带上下文的消息列表
     */
    private List<Map<String, String>> buildContextMessages(Long conversationId, String currentMessage) {
        List<Map<String, String>> messages = new ArrayList<>();

        // System prompt
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));

        // Recent context messages (max 20 rounds)
        List<AIChatMessage> recentMessages = chatMessageService.getRecentMessages(conversationId, MAX_CONTEXT_ROUNDS);
        for (AIChatMessage msg : recentMessages) {
            messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
        }

        // Current user message is already saved, so it's included in recentMessages

        return messages;
    }

    @Override
    public AIConversationMessagesResponse getConversationMessages(Long userId, Long conversationId) {
        AIConversation conv = getById(conversationId);
        if (conv == null || !conv.getUserId().equals(userId)) {
            throw new BusinessException("对话不存在或无权访问");
        }

        List<AIChatMessage> messages = chatMessageService.getMessages(conversationId);
        int totalTokens = chatMessageService.getTotalTokens(conversationId);
        int messageCount = messages.size();
        int usedRounds = messageCount / 2;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<AIConversationMessagesResponse.MessageItem> items = messages.stream()
                .map(m -> AIConversationMessagesResponse.MessageItem.builder()
                        .id(m.getId())
                        .role(m.getRole())
                        .content(m.getContent())
                        .tokenCount(m.getTokenCount())
                        .createdAt(m.getCreatedAt() != null ? m.getCreatedAt().format(fmt) : null)
                        .build())
                .toList();

        return AIConversationMessagesResponse.builder()
                .conversationId(conversationId)
                .messages(items)
                .messageCount(messageCount)
                .totalTokens(totalTokens)
                .maxRounds(MAX_CONTEXT_ROUNDS)
                .usedRounds(usedRounds)
                .build();
    }

    @Override
    public void clearConversationMessages(Long userId, Long conversationId) {
        AIConversation conv = getById(conversationId);
        if (conv == null || !conv.getUserId().equals(userId)) {
            throw new BusinessException("对话不存在或无权访问");
        }
        chatMessageService.clearMessages(conversationId);
        log.info("用户 {} 清除了对话 {} 的上下文", userId, conversationId);
    }

    /**
     * 生成对话标题
     */
    private String generateConversationTitle(String firstMessage) {
        // 简单截取前20个字符作为标题
        if (firstMessage == null || firstMessage.isEmpty()) {
            return "新对话";
        }
        String title = firstMessage.length() > 20 ? firstMessage.substring(0, 20) + "..." : firstMessage;
        return title;
    }

    /**
     * 构建生成提示词
     */
    private String buildPromptForGenerate(String type, Note note, String customPrompt) {
        String content = note.getContent();
        String title = note.getTitle();

        StringBuilder prompt = new StringBuilder();

        switch (type.toLowerCase()) {
            case "summarize":
                prompt.append("请为以下笔记生成一个摘要：\n\n");
                prompt.append("标题：").append(title).append("\n\n");
                prompt.append("内容：\n").append(content);
                break;

            case "optimize":
                prompt.append("请优化以下笔记，使其更加清晰、有条理：\n\n");
                prompt.append("标题：").append(title).append("\n\n");
                prompt.append("内容：\n").append(content);
                break;

            case "expand":
                prompt.append("请扩写以下笔记，补充更多细节和内容：\n\n");
                prompt.append("标题：").append(title).append("\n\n");
                prompt.append("内容：\n").append(content);
                break;

            case "rewrite":
                prompt.append("请改写以下笔记，保持原意但使用不同的表达方式：\n\n");
                prompt.append("标题：").append(title).append("\n\n");
                prompt.append("内容：\n").append(content);
                break;

            case "continue":
                prompt.append("请续写以下笔记：\n\n");
                prompt.append("标题：").append(title).append("\n\n");
                prompt.append("内容：\n").append(content);
                break;

            default:
                if (customPrompt != null && !customPrompt.isEmpty()) {
                    prompt.append(customPrompt).append("\n\n");
                    prompt.append("笔记标题：").append(title).append("\n\n");
                    prompt.append("笔记内容：\n").append(content);
                } else {
                    prompt.append("请处理以下笔记：\n\n");
                    prompt.append("标题：").append(title).append("\n\n");
                    prompt.append("内容：\n").append(content);
                }
                break;
        }

        return prompt.toString();
    }

    @Override
    public String summarize(Long userId, Long noteId, String content) {
        String text = resolveContent(userId, noteId, content);
        if (text == null || text.isBlank()) {
            throw new BusinessException("笔记内容为空，无法总结");
        }
        return callAssistant(userId, "你是一个笔记助手。请用简洁的中文总结以下笔记的核心内容，不超过200字。", text);
    }

    @Override
    public String outline(Long userId, Long noteId, String content) {
        String text = resolveContent(userId, noteId, content);
        if (text == null || text.isBlank()) {
            throw new BusinessException("笔记内容为空，无法生成大纲");
        }
        return callAssistant(userId, "你是一个笔记助手。请从以下笔记内容中提取结构化大纲，使用Markdown格式。", text);
    }

    @Override
    public String continueWrite(Long userId, String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException("内容为空，无法续写");
        }
        return callAssistant(userId, "你是一个写作助手。请根据以下笔记内容，自然地续写200字左右。不要重复已有内容。", content);
    }

    @Override
    public String translate(Long userId, String content, String targetLang) {
        if (content == null || content.isBlank()) {
            throw new BusinessException("内容为空，无法翻译");
        }
        String systemPrompt = "zh".equals(targetLang)
                ? "你是一个翻译助手。请将以下英文翻译为中文，只输出翻译结果，不要添加解释。"
                : "你是一个翻译助手。请将以下中文翻译为英文，只输出翻译结果，不要添加解释。";
        return callAssistant(userId, systemPrompt, content);
    }

    @Override
    public String polish(Long userId, String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException("内容为空，无法润色");
        }
        return callAssistant(userId, "你是一个文字编辑助手。请优化以下文字的表达，使其更流畅、更专业，保持原意不变。只输出润色后的文字，不要添加解释。", content);
    }

    @Override
    public List<String> extractTasks(Long userId, String content) {
        if (content == null || content.isBlank()) {
            return new ArrayList<>();
        }
        String res = callAssistant(userId, "你是一个任务管理助手。请从以下文字中提取所有的待办事项(TODO)，每行一个，以'- '开头。如果没有待办事项，请输出'无'。", content);
        if (res.contains("无") || res.isBlank()) return new ArrayList<>();
        return java.util.Arrays.stream(res.split("\n"))
                .map(String::trim)
                .filter(s -> s.startsWith("-"))
                .map(s -> s.substring(1).trim())
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @Override
    public List<Note> suggestRelations(Long userId, Long noteId) {
        return knowledgeBaseService.findSimilarNotes(userId, noteId, 5, java.util.Map.of());
    }

    @Override
    public String transcribe(Long userId, byte[] audioData, String fileName) {
        String apiKey = System.getenv("WHISPER_API_KEY");
        String baseUrl = System.getenv("WHISPER_BASE_URL");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = aiConfig.getDeepseek().getApiKey();
        }
        if (apiKey == null || apiKey.isEmpty()) {
            throw new BusinessException("未配置语音转写 API Key");
        }
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(apiKey);
            org.springframework.util.LinkedMultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
            org.springframework.core.io.ByteArrayResource resource = new org.springframework.core.io.ByteArrayResource(audioData) {
                @Override
                public String getFilename() { return fileName; }
            };
            body.add("file", resource);
            body.add("model", "whisper-1");
            org.springframework.http.HttpEntity<org.springframework.util.LinkedMultiValueMap<String, Object>> requestEntity = new org.springframework.http.HttpEntity<>(body, headers);
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            String url = (baseUrl != null ? baseUrl : "https://api.openai.com/v1") + "/audio/transcriptions";
            com.fasterxml.jackson.databind.JsonNode response = restTemplate.postForObject(url, requestEntity, com.fasterxml.jackson.databind.JsonNode.class);
            if (response != null && response.has("text")) {
                return response.get("text").asText();
            }
            return "";
        } catch (Exception e) {
            log.error("语音转写失败: {}", e.getMessage());
            throw new BusinessException("语音转写失败: " + e.getMessage());
        }
    }

    @Override
    public String ocr(Long userId, byte[] imageData, String fileName) {
        String apiKey = System.getenv("VISION_API_KEY");
        String baseUrl = System.getenv("VISION_BASE_URL");
        
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = aiConfig.getDeepseek().getApiKey(); // DeepSeek may support vision in some endpoints
        }
        
        if (apiKey == null || apiKey.isEmpty()) {
            throw new BusinessException("未配置 Vision/OCR API Key");
        }

        try {
            String base64Image = java.util.Base64.getEncoder().encodeToString(imageData);
            String mimeType = fileName.endsWith(".png") ? "image/png" : "image/jpeg";
            
            Map<String, Object> message = Map.of(
                "role", "user",
                "content", List.of(
                    Map.of("type", "text", "text", "请提取图片中的所有文字，并将其转换为整洁的 Markdown 格式。只输出提取后的文字内容，不要输出任何解释。"),
                    Map.of("type", "image_url", "image_url", Map.of("url", "data:" + mimeType + ";base64," + base64Image))
                )
            );

            Map<String, Object> body = Map.of(
                "model", "gpt-4o", // 或者兼容的模型
                "messages", List.of(message),
                "max_tokens", 2000
            );

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            org.springframework.http.HttpEntity<Map<String, Object>> requestEntity = new org.springframework.http.HttpEntity<>(body, headers);
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            
            String url = (baseUrl != null ? baseUrl : "https://api.openai.com/v1") + "/chat/completions";
            com.fasterxml.jackson.databind.JsonNode response = restTemplate.postForObject(url, requestEntity, com.fasterxml.jackson.databind.JsonNode.class);
            
            if (response != null && response.has("choices")) {
                return response.get("choices").get(0).get("message").get("content").asText();
            }
            return "";
        } catch (Exception e) {
            log.error("OCR 失败: {}", e.getMessage());
            throw new BusinessException("OCR 识图失败: " + e.getMessage());
        }
    }

    private String transcribeWithApiKey(Long userId, byte[] audioData, String fileName, String apiKey, String baseUrl) {
        // (Moved the previous transcribe logic here or keep it as is)
        // I'll just keep the original transcribe and add ocr
        return ""; // placeholder
    }

    @Override
    public List<String> suggestTags(Long userId, String content) {
        if (content == null || content.isBlank()) {
            return new ArrayList<>();
        }
        String res = callAssistant(userId, "你是一个标签建议助手。请根据以下笔记内容，提供3-5个最相关的标签。只输出标签，用英文逗号分隔，不要输出任何解释。", content);
        if (res.isBlank()) return new ArrayList<>();
        return java.util.Arrays.stream(res.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @Override
    public List<Map<String, Object>> getFlashback(Long userId) {
        List<Note> notes = knowledgeBaseService.getFlashbackNotes(userId, 3);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Note note : notes) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("note", note);
            
            // 使用 AI 生成一段简短的召回理由
            try {
                String reason = callAssistant(userId, 
                    "你是一个知识唤醒助手。请根据这篇笔记的内容，给用户一个简短（20字以内）的回顾理由，告诉他为什么今天值得重新看一眼这篇笔记。直接输出理由，不要有废话。", 
                    note.getTitle() + "\n" + note.getContent().substring(0, Math.min(200, note.getContent().length()))
                );
                map.put("reason", reason);
            } catch (Exception e) {
                map.put("reason", "重温旧知，开启新灵感。");
            }
            
            result.add(map);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> detectConflicts(Long userId) {
        List<Note> recentNotes = noteService.recentNotes(userId, 5);
        List<Map<String, Object>> conflicts = new ArrayList<>();
        java.util.Set<String> processedPairs = new java.util.HashSet<>();

        for (Note note1 : recentNotes) {
            // 找语义非常接近的笔记 (限制 2 篇，高阈值)
            List<Note> similarNotes = knowledgeBaseService.findSimilarNotes(userId, note1.getId(), 2, Map.of());
            
            for (Note note2 : similarNotes) {
                String pairId = note1.getId() < note2.getId() ? note1.getId() + "-" + note2.getId() : note2.getId() + "-" + note1.getId();
                if (processedPairs.contains(pairId)) continue;
                processedPairs.add(pairId);

                try {
                    String prompt = String.format(
                        "请分析以下两篇笔记的内容，判断它们是否存在【冲突】或【过度重复】。\\n" +
                        "笔记A标题：%s\\n内容：%s\\n\\n" +
                        "笔记B标题：%s\\n内容：%s\\n\\n" +
                        "如果存在冲突或重复，请给出简短的分析并建议如何处理。如果一切正常，请回复'正常'。",
                        note1.getTitle(), note1.getContent().substring(0, Math.min(300, note1.getContent().length())),
                        note2.getTitle(), note2.getContent().substring(0, Math.min(300, note2.getContent().length()))
                    );

                    String analysis = callAssistant(userId, "你是一个知识库质量审计助手。你的任务是发现笔记间的逻辑冲突或内容冗余。", prompt);
                    
                    if (!analysis.contains("正常")) {
                        Map<String, Object> conflict = new java.util.HashMap<>();
                        conflict.put("noteA", Map.of("id", note1.getId(), "title", note1.getTitle()));
                        conflict.put("noteB", Map.of("id", note2.getId(), "title", note2.getTitle()));
                        conflict.put("analysis", analysis);
                        conflicts.add(conflict);
                    }
                } catch (Exception e) {
                    log.warn("冲突检测失败: {}", e.getMessage());
                }
            }
        }
        return conflicts;
    }

    /**
     * 根据noteId或content获取笔记文本
     */
    private String resolveContent(Long userId, Long noteId, String content) {
        if (content != null && !content.isBlank()) {
            return content;
        }
        if (noteId != null) {
            Note note = noteService.getNoteDetail(userId, noteId);
            return note != null ? note.getContent() : null;
        }
        return null;
    }

    /**
     * 通用AI辅助调用（使用用户配置的默认provider）
     */
    private String callAssistant(Long userId, String systemPrompt, String userContent) {
        AIProvider provider = getProviderWithApiKey(userId, null);
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userContent));
        return provider.chat(provider.getDefaultModel(), messages);
    }

    @Override
    public boolean testConnection(Long userId, String providerName, String apiKey, String model) {
        try {
            AIProvider provider = providerFactory.getProvider(providerName);
            // Use provided apiKey or fall back to configured one
            if (apiKey != null && !apiKey.isEmpty()) {
                if (provider instanceof DeepSeekProvider) {
                    ((DeepSeekProvider) provider).setApiKey(apiKey);
                } else if (provider instanceof GLMProvider) {
                    ((GLMProvider) provider).setApiKey(apiKey);
                } else if (provider instanceof MiniMaxProvider) {
                    ((MiniMaxProvider) provider).setApiKey(apiKey);
                }
            }
            // Send a simple test message
            List<Map<String, String>> messages = List.of(
                Map.of("role", "user", "content", "Hi")
            );
            provider.chat(model != null ? model : provider.getDefaultModel(), messages);
            return true;
        } catch (Exception e) {
            log.warn("AI连接测试失败: {}", e.getMessage());
            return false;
        }
    }

}
