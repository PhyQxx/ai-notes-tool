package com.ainotes.entity;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI对话实体类
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Data
@TableName("t_ai_conversation")
public class AIConversation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 对话ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 关联笔记ID
     */
    private Long noteId;

    /**
     * AI提供商：deepseek-DeepSeek，glm-智谱GLM
     */
    private String aiProvider;

    /**
     * AI模型名称
     */
    private String aiModel;

    /**
     * 对话消息(JSON数组)
     */
    private String messages;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 获取消息列表
     *
     * @return 消息列表
     */
    @SuppressWarnings("unchecked")
    public List<AiMessage> getMessageList() {
        if (messages == null || messages.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return JSON.parseArray(messages, AiMessage.class);
    }

    /**
     * 设置消息列表
     *
     * @param messageList 消息列表
     */
    public void setMessageList(List<AiMessage> messageList) {
        this.messages = JSON.toJSONString(messageList);
    }

    /**
     * AI消息内部类
     */
    @Data
    public static class AiMessage implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 消息角色：system-系统，user-用户，assistant-助手
         */
        private String role;

        /**
         * 消息内容
         */
        private String content;

        /**
         * 时间戳
         */
        private Long timestamp;

        public AiMessage() {
            this.timestamp = System.currentTimeMillis();
        }

        public AiMessage(String role, String content) {
            this.role = role;
            this.content = content;
            this.timestamp = System.currentTimeMillis();
        }
    }

}
