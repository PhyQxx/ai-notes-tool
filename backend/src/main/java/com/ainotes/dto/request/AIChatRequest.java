package com.ainotes.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI对话请求DTO
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Data
public class AIChatRequest {

    /**
     * 关联笔记ID（可选）
     */
    private Long noteId;

    /**
     * AI提供商：deepseek-DeepSeek，glm-智谱GLM
     */
    @NotBlank(message = "AI提供商不能为空")
    private String provider;

    /**
     * AI模型名称
     */
    @NotBlank(message = "AI模型不能为空")
    private String model;

    /**
     * 用户消息
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /**
     * 对话ID（可选，用于继续对话）
     */
    private Long conversationId;

}
