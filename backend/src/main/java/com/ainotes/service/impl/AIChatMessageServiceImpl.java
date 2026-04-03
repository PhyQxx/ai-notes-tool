package com.ainotes.service.impl;

import com.ainotes.entity.AIChatMessage;
import com.ainotes.mapper.AIChatMessageMapper;
import com.ainotes.service.AIChatMessageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AIChatMessageServiceImpl implements AIChatMessageService {

    private final AIChatMessageMapper mapper;

    @Override
    public AIChatMessage saveMessage(Long conversationId, String role, String content) {
        AIChatMessage msg = new AIChatMessage();
        msg.setConversationId(conversationId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setTokenCount(AIChatMessage.estimateTokens(content));
        msg.setCreatedAt(LocalDateTime.now());
        mapper.insert(msg);
        return msg;
    }

    @Override
    public List<AIChatMessage> getMessages(Long conversationId) {
        return mapper.selectList(new LambdaQueryWrapper<AIChatMessage>()
                .eq(AIChatMessage::getConversationId, conversationId)
                .orderByAsc(AIChatMessage::getCreatedAt));
    }

    @Override
    public List<AIChatMessage> getRecentMessages(Long conversationId, int maxRounds) {
        int maxMessages = maxRounds * 2; // 1 round = 1 user + 1 assistant
        List<AIChatMessage> all = getMessages(conversationId);
        if (all.size() <= maxMessages) {
            return all;
        }
        return all.subList(all.size() - maxMessages, all.size());
    }

    @Override
    public int getTotalTokens(Long conversationId) {
        List<AIChatMessage> messages = getMessages(conversationId);
        return messages.stream().mapToInt(m -> m.getTokenCount() != null ? m.getTokenCount() : 0).sum();
    }

    @Override
    public int getMessageCount(Long conversationId) {
        return Math.toIntExact(mapper.selectCount(new LambdaQueryWrapper<AIChatMessage>()
                .eq(AIChatMessage::getConversationId, conversationId)));
    }

    @Override
    public void clearMessages(Long conversationId) {
        mapper.delete(new LambdaQueryWrapper<AIChatMessage>()
                .eq(AIChatMessage::getConversationId, conversationId));
    }

    @Override
    public void deleteByConversationId(Long conversationId) {
        clearMessages(conversationId);
    }
}
