package com.ainotes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * AI内容生成请求DTO
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Data
public class AIGenerateRequest {

    /**
     * 笔记ID
     */
    @NotNull(message = "笔记ID不能为空")
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
     * 自定义提示词（可选）
     */
    private String prompt;

    /**
     * 生成类型：summarize-摘要，optimize-优化，expand-扩写，rewrite-改写，continue-续写
     */
    @NotBlank(message = "生成类型不能为空")
    private String type;

}
