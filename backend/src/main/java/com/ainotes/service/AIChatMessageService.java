package com.ainotes.service;

import com.ainotes.entity.AIChatMessage;

import java.util.List;

public interface AIChatMessageService {

    /**
     * 保存消息
     */
    AIChatMessage saveMessage(Long conversationId, String role, String content);

    /**
     * 获取对话的消息列表（按时间正序）
     */
    List<AIChatMessage> getMessages(Long conversationId);

    /**
     * 获取对话的最近N轮消息用于上下文（1轮 = 1 user + 1 assistant）
     */
    List<AIChatMessage> getRecentMessages(Long conversationId, int maxRounds);

    /**
     * 获取对话的总 token 数
     */
    int getTotalTokens(Long conversationId);

    /**
     * 获取对话的消息数量
     */
    int getMessageCount(Long conversationId);

    /**
     * 清除对话所有消息
     */
    void clearMessages(Long conversationId);

    /**
     * 删除对话所有消息
     */
    void deleteByConversationId(Long conversationId);
}
