package com.ainotes.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI对话响应DTO
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIChatResponse {

    /**
     * 对话ID
     */
    private Long conversationId;

    /**
     * 消息内容
     */
    private String message;

    /**
     * 消息角色：user-用户，assistant-助手
     */
    private String role;

    /**
     * 对话标题（首次对话时生成）
     */
    private String title;

}
