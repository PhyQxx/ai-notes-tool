package com.ainotes.service;

import com.ainotes.dto.request.AIChatRequest;
import com.ainotes.dto.request.AIGenerateRequest;
import com.ainotes.dto.request.AIConfigUpdateRequest;
import com.ainotes.dto.response.AIChatResponse;
import com.ainotes.dto.response.AIConfigResponse;
import com.ainotes.dto.response.AIConversationMessagesResponse;
import com.ainotes.entity.AIConversation;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI服务接口
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
public interface AIService {

    /**
     * AI对话（同步）
     *
     * @param userId  用户ID
     * @param request 对话请求
     * @return 对话响应
     */
    AIChatResponse chat(Long userId, AIChatRequest request);

    /**
     * AI流式对话（SSE）
     *
     * @param userId  用户ID
     * @param request 对话请求
     * @return SSE响应
     */
    SseEmitter chatStream(Long userId, AIChatRequest request);

    /**
     * AI内容生成
     *
     * @param userId  用户ID
     * @param request 生成请求
     * @return 生成内容
     */
    String generate(Long userId, AIGenerateRequest request);

    /**
     * 获取用户的对话列表（分页）
     *
     * @param userId   用户ID
     * @param page     页码
     * @param pageSize 每页大小
     * @return 对话列表
     */
    IPage<AIConversation> getConversations(Long userId, Integer page, Integer pageSize);

    /**
     * 获取对话详情
     *
     * @param userId          用户ID
     * @param conversationId  对话ID
     * @return 对话详情
     */
    AIConversation getConversationDetail(Long userId, Long conversationId);

    /**
     * 删除对话
     *
     * @param userId         用户ID
     * @param conversationId 对话ID
     */
    void deleteConversation(Long userId, Long conversationId);

    /**
     * 重命名对话
     */
    void renameConversation(Long userId, Long conversationId, String title);

    /**
     * 新建对话
     */
    AIConversation createConversation(Long userId, Long noteId, String provider, String model);

    /**
     * 获取用户AI配置
     *
     * @param userId 用户ID
     * @return AI配置信息
     */
    AIConfigResponse getConfig(Long userId);

    /**
     * 更新用户AI配置
     *
     * @param userId  用户ID
     * @param request 配置更新请求
     */
    void updateConfig(Long userId, AIConfigUpdateRequest request);

    /**
     * 获取所有可用的提供商列表
     *
     * @return 提供商信息列表
     */
    List<AIConfigResponse.ProviderInfo> getProviders();

    /**
     * 获取对话消息列表（含 token 统计）
     */
    AIConversationMessagesResponse getConversationMessages(Long userId, Long conversationId);

    /**
     * 清除对话上下文
     */
    void clearConversationMessages(Long userId, Long conversationId);

    /**
     * AI总结笔记
     */
    String summarize(Long userId, Long noteId, String content);

    /**
     * AI生成大纲
     */
    String outline(Long userId, Long noteId, String content);

    /**
     * AI续写
     */
    String continueWrite(Long userId, String content);

    /**
     * AI翻译
     */
    String translate(Long userId, String content, String targetLang);

    /**
     * AI润色
     */
    String polish(Long userId, String content);

    /**
     * 测试AI连接
     *
     * @param userId  用户ID
     * @param provider 提供商名称
     * @param apiKey  API密钥（可选，为空则使用配置）
     * @param model   测试用模型
     * @return 是否连接成功
     */
    boolean testConnection(Long userId, String provider, String apiKey, String model);

}
