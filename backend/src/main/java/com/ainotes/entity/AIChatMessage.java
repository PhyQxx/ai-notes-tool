package com.ainotes.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI对话消息实体类
 */
@Data
@TableName("t_ai_chat_message")
public class AIChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long conversationId;

    /**
     * 消息角色：user/assistant/system
     */
    private String role;

    private String content;

    /**
     * token 数量
     */
    private Integer tokenCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 估算 token 数（中文约 1.5 字/token，英文约 4 字符/token）
     */
    public static int estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0;
        int chinese = 0, other = 0;
        for (char c : text.toCharArray()) {
            if (c >= 0x4E00 && c <= 0x9FFF) {
                chinese++;
            } else {
                other++;
            }
        }
        return (int) Math.ceil(chinese / 1.5 + other / 4.0);
    }
}
