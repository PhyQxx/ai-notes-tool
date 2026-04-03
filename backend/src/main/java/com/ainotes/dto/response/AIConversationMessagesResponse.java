package com.ainotes.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AIConversationMessagesResponse {
    private Long conversationId;
    private List<MessageItem> messages;
    private int messageCount;
    private int totalTokens;
    private int maxRounds;
    private int usedRounds;

    @Data
    @Builder
    public static class MessageItem {
        private Long id;
        private String role;
        private String content;
        private Integer tokenCount;
        private String createdAt;
    }
}
