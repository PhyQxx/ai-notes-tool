package com.ainotes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ainotes.dto.request.AIChatRequest;
import com.ainotes.dto.request.AIGenerateRequest;
import com.ainotes.dto.request.AIConfigUpdateRequest;
import com.ainotes.dto.response.AIChatResponse;
import com.ainotes.dto.response.AIConfigResponse;
import com.ainotes.dto.response.AIConversationMessagesResponse;
import com.ainotes.entity.AIConversation;
import com.ainotes.service.AIService;
import com.ainotes.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * AI智能体控制器
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Validated
@Tag(name = "AI智能体", description = "AI对话、内容生成、配置管理等操作")
public class AIController {

    private final AIService aiService;

    /**
     * AI对话（同步）
     *
     * @param request 对话请求
     * @return 对话响应
     */
    @PostMapping("/chat")
    @Operation(summary = "AI对话")
    public Result<AIChatResponse> chat(@Valid @RequestBody AIChatRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        AIChatResponse response = aiService.chat(userId, request);
        return Result.success(response);
    }

    /**
     * AI流式对话（SSE）
     *
     * @param request 对话请求
     * @return SSE流式响应
     */
    @PostMapping("/chat/stream")
    @Operation(summary = "AI流式对话（SSE）")
    public SseEmitter chatStream(@Valid @RequestBody AIChatRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return aiService.chatStream(userId, request);
    }

    /**
     * AI内容生成
     *
     * @param request 生成请求
     * @return 生成内容
     */
    @PostMapping("/generate")
    @Operation(summary = "AI内容生成")
    public Result<Map<String, String>> generate(@Valid @RequestBody AIGenerateRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        String content = aiService.generate(userId, request);
        return Result.success(Map.of("content", content));
    }

    /**
     * 获取对话列表
     *
     * @param page     页码
     * @param pageSize 每页大小
     * @return 对话列表
     */
    @GetMapping("/conversations")
    @Operation(summary = "获取对话列表")
    public Result<IPage<AIConversation>> getConversations(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        IPage<AIConversation> conversations = aiService.getConversations(userId, page, pageSize);
        return Result.success(conversations);
    }

    /**
     * 获取对话详情
     *
     * @param id 对话ID
     * @return 对话详情
     */
    @GetMapping("/conversations/{id}")
    @Operation(summary = "获取对话详情")
    public Result<AIConversation> getConversationDetail(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        AIConversation conversation = aiService.getConversationDetail(userId, id);
        return Result.success(conversation);
    }

    /**
     * 删除对话
     *
     * @param id 对话ID
     * @return 成功信息
     */
    @DeleteMapping("/conversations/{id}")
    @Operation(summary = "删除对话")
    public Result<Void> deleteConversation(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        aiService.deleteConversation(userId, id);
        return Result.success("删除成功", null);
    }

    /**
     * 重命名对话
     */
    @PutMapping("/conversations/{id}/rename")
    @Operation(summary = "重命名对话")
    public Result<Void> renameConversation(@PathVariable Long id, @RequestBody Map<String, String> body, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        aiService.renameConversation(userId, id, body.get("title"));
        return Result.success("重命名成功", null);
    }

    /**
     * 新建对话
     */
    @PostMapping("/conversations")
    @Operation(summary = "新建对话")
    public Result<AIConversation> createConversation(@RequestBody(required = false) Map<String, Object> body, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Long noteId = body != null && body.containsKey("noteId") ? Long.valueOf(body.get("noteId").toString()) : null;
        String provider = body != null && body.containsKey("provider") ? body.get("provider").toString() : "deepseek";
        String model = body != null && body.containsKey("model") ? body.get("model").toString() : "deepseek-chat";
        AIConversation conv = aiService.createConversation(userId, noteId, provider, model);
        return Result.success(conv);
    }

    /**
     * 获取AI配置
     *
     * @return AI配置信息
     */
    @GetMapping("/config")
    @Operation(summary = "获取AI配置")
    public Result<AIConfigResponse> getConfig(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        AIConfigResponse config = aiService.getConfig(userId);
        return Result.success(config);
    }

    /**
     * 更新AI配置
     *
     * @param request 配置更新请求
     * @return 成功信息
     */
    @PutMapping("/config")
    @Operation(summary = "更新AI配置")
    public Result<Void> updateConfig(@Valid @RequestBody AIConfigUpdateRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        aiService.updateConfig(userId, request);
        return Result.success("配置更新成功", null);
    }

    /**
     * 获取可用的提供商列表
     *
     * @return 提供商列表
     */
    @GetMapping("/providers")
    @Operation(summary = "获取可用的提供商列表")
    public Result<List<AIConfigResponse.ProviderInfo>> getProviders() {
        List<AIConfigResponse.ProviderInfo> providers = aiService.getProviders();
        return Result.success(providers);
    }

    /**
     * 获取对话消息列表（含上下文统计）
     */
    @GetMapping("/conversations/{id}/messages")
    @Operation(summary = "获取对话消息列表")
    public Result<AIConversationMessagesResponse> getConversationMessages(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(aiService.getConversationMessages(userId, id));
    }

    /**
     * 清除对话上下文（重新开始）
     */
    @DeleteMapping("/conversations/{id}/messages")
    @Operation(summary = "清除对话上下文")
    public Result<Void> clearConversationMessages(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        aiService.clearConversationMessages(userId, id);
        return Result.success("上下文已清除", null);
    }

    // ==================== AI 辅助写作接口 ====================

    /**
     * AI总结笔记
     */
    @PostMapping("/assistant/summarize")
    @Operation(summary = "AI总结笔记")
    public Result<Map<String, String>> summarize(@RequestBody Map<String, Object> body, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Long noteId = body.get("noteId") != null ? Long.valueOf(body.get("noteId").toString()) : null;
        String content = body.get("content") != null ? body.get("content").toString() : null;
        String result = aiService.summarize(userId, noteId, content);
        return Result.success(Map.of("data", result));
    }

    /**
     * AI生成大纲
     */
    @PostMapping("/assistant/outline")
    @Operation(summary = "AI生成大纲")
    public Result<Map<String, String>> outline(@RequestBody Map<String, Object> body, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Long noteId = body.get("noteId") != null ? Long.valueOf(body.get("noteId").toString()) : null;
        String content = body.get("content") != null ? body.get("content").toString() : null;
        String result = aiService.outline(userId, noteId, content);
        return Result.success(Map.of("data", result));
    }

    /**
     * AI续写
     */
    @PostMapping("/assistant/continue")
    @Operation(summary = "AI续写")
    public Result<Map<String, String>> continueWrite(@RequestBody Map<String, Object> body, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        String content = body.get("content").toString();
        String result = aiService.continueWrite(userId, content);
        return Result.success(Map.of("data", result));
    }

    /**
     * AI翻译
     */
    @PostMapping("/assistant/translate")
    @Operation(summary = "AI翻译（中英互译）")
    public Result<Map<String, String>> translate(@RequestBody Map<String, Object> body, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        String content = body.get("content").toString();
        String targetLang = body.get("targetLang").toString();
        String result = aiService.translate(userId, content, targetLang);
        return Result.success(Map.of("data", result));
    }

    /**
     * AI润色
     */
    @PostMapping("/assistant/polish")
    @Operation(summary = "AI润色")
    public Result<Map<String, String>> polish(@RequestBody Map<String, Object> body, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        String content = body.get("content").toString();
        String result = aiService.polish(userId, content);
        return Result.success(Map.of("data", result));
    }

}
