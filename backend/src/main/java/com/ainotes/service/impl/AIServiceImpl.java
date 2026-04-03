package com.ainotes.service.impl;

import com.ainotes.ai.AIProvider;
import com.ainotes.ai.AIProviderFactory;
import com.ainotes.common.exception.BusinessException;
import com.ainotes.dto.request.AIChatRequest;
import com.ainotes.dto.request.AIConfigUpdateRequest;
import com.ainotes.dto.request.AIGenerateRequest;
import com.ainotes.dto.response.AIChatResponse;
import com.ainotes.dto.response.AIConfigResponse;
import com.ainotes.entity.AIConversation;
import com.ainotes.entity.AIConversation.AiMessage;
import com.ainotes.entity.Note;
import com.ainotes.mapper.AIConversationMapper;
import com.ainotes.service.AIService;
import com.ainotes.interceptor.PromptInjectionInterceptor;
import com.ainotes.service.NoteService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String AI_CONFIG_KEY_PREFIX = "ai:config:";
    private static final Duration CONFIG_CACHE_TTL = Duration.ofHours(1);

    @Override
    public AIChatResponse chat(Long userId, AIChatRequest request) {
        if (PromptInjectionInterceptor.containsInjection(request.getMessage())) {
            throw new BusinessException("消息包含不安全内容，请修改后重试");
        }
        // 获取AI Provider
        AIProvider provider = getProviderWithApiKey(userId, request.getProvider());

        // 获取或创建对话
        AIConversation conversation = getOrCreateConversation(userId, request);

        // 构建消息列表
        List<Map<String, String>> messages = buildMessagesForChat(conversation, request.getMessage());

        // 调用AI服务
        String aiResponse = provider.chat(request.getModel(), messages);

        // 保存消息到对话
        saveChatMessages(conversation, request.getMessage(), aiResponse);

        // 构建响应
        AIChatResponse response = new AIChatResponse();
        response.setConversationId(conversation.getId());
        response.setMessage(aiResponse);
        response.setRole("assistant");
        response.setTitle(conversation.getMessageList().isEmpty() ? null : generateConversationTitle(request.getMessage()));

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
        SseEmitter emitter = new SseEmitter(300_000L); // 5min timeout
        AIProvider provider = getProviderWithApiKey(userId, request.getProvider());
        AIConversation conversation = getOrCreateConversation(userId, request);
        List<Map<String, String>> messages = buildMessagesForChat(conversation, request.getMessage());

        StringBuilder fullResponse = new StringBuilder();

        emitter.onCompletion(() -> {});
        emitter.onTimeout(emitter::complete);
        emitter.onError(e -> emitter.complete());

        // Save user message immediately
        conversation.getMessageList().add(new AIConversation.AiMessage("user", request.getMessage()));

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
                    // Save assistant message
                    conversation.getMessageList().add(new AIConversation.AiMessage("assistant", fullResponse.toString()));
                    if (conversation.getMessageList().size() > 20) {
                        conversation.setMessageList(conversation.getMessageList().subList(
                                conversation.getMessageList().size() - 20, conversation.getMessageList().size()));
                    }
                    updateById(conversation);
                    try {
                        emitter.send(SseEmitter.event().data("{\"conversationId\":" + conversation.getId() + ",\"done\":true}", org.springframework.http.MediaType.APPLICATION_JSON));
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
        // 从缓存获取用户配置
        String configKey = AI_CONFIG_KEY_PREFIX + userId;
        Map<String, Object> config = (Map<String, Object>) redisTemplate.opsForValue().get(configKey);

        String provider = "deepseek";
        String model = "deepseek-chat";

        if (config != null) {
            provider = (String) config.getOrDefault("provider", "deepseek");
            model = (String) config.getOrDefault("model", "deepseek-chat");
        }

        // 获取提供商信息
        AIProvider currentProvider = providerFactory.getProvider(provider);
        List<AIConfigResponse.ProviderInfo> providers = providerFactory.getProviderInfos();

        return AIConfigResponse.builder()
                .provider(provider)
                .model(model)
                .providers(providers)
                .models(currentProvider.getSupportedModels())
                .build();
    }

    @Override
    public void updateConfig(Long userId, AIConfigUpdateRequest request) {
        // 验证提供商和模型
        AIProvider provider = providerFactory.getProvider(request.getProvider());
        if (!provider.getSupportedModels().contains(request.getModel())) {
            throw new BusinessException("不支持的模型: " + request.getModel());
        }

        // 构建配置
        Map<String, Object> config = new HashMap<>();
        config.put("provider", request.getProvider());
        config.put("model", request.getModel());
        config.put("deepseekApiKey", request.getDeepseekApiKey());
        config.put("glmApiKey", request.getGlmApiKey());

        // 缓存配置
        String configKey = AI_CONFIG_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(configKey, config, CONFIG_CACHE_TTL);

        log.info("用户 {} 更新AI配置: provider={}, model={}", userId, request.getProvider(), request.getModel());
    }

    @Override
    public List<AIConfigResponse.ProviderInfo> getProviders() {
        return providerFactory.getProviderInfos();
    }

    /**
     * 获取AI Provider（带API Key）
     */
    private AIProvider getProviderWithApiKey(Long userId, String providerName) {
        String configKey = AI_CONFIG_KEY_PREFIX + userId;
        Map<String, Object> config = (Map<String, Object>) redisTemplate.opsForValue().get(configKey);

        String apiKey = null;
        if (config != null) {
            if ("deepseek".equalsIgnoreCase(providerName)) {
                apiKey = (String) config.get("deepseekApiKey");
            } else if ("glm".equalsIgnoreCase(providerName)) {
                apiKey = (String) config.get("glmApiKey");
            }
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
     * 构建对话消息列表
     */
    private List<Map<String, String>> buildMessagesForChat(AIConversation conversation, String userMessage) {
        List<Map<String, String>> messages = new ArrayList<>();

        // 添加历史消息
        List<AiMessage> historyMessages = conversation.getMessageList();
        for (AiMessage msg : historyMessages) {
            messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
        }

        // 添加当前用户消息
        messages.add(Map.of("role", "user", "content", userMessage));

        return messages;
    }

    /**
     * 保存对话消息
     */
    private void saveChatMessages(AIConversation conversation, String userMessage, String aiMessage) {
        List<AiMessage> messages = conversation.getMessageList();

        // 添加用户消息
        messages.add(new AiMessage("user", userMessage));

        // 添加AI回复
        messages.add(new AiMessage("assistant", aiMessage));

        // 限制消息数量（保留最近20条）
        if (messages.size() > 20) {
            messages = messages.subList(messages.size() - 20, messages.size());
        }

        conversation.setMessageList(messages);
        updateById(conversation);
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

}
