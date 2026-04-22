package com.ainotes.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI配置更新请求DTO
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Data
public class AIConfigUpdateRequest {

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
     * DeepSeek API密钥（可选）
     */
    private String deepseekApiKey;

    /**
     * GLM API密钥（可选）
     */
    private String glmApiKey;

    /**
     * MiniMax API密钥（可选）
     */
    private String minimaxApiKey;

}
