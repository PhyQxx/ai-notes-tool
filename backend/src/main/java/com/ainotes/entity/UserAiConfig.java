package com.ainotes.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户AI配置实体类
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Data
@TableName("t_user_ai_config")
public class UserAiConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * AI提供商：deepseek / glm
     */
    private String provider;

    /**
     * AI模型名称
     */
    private String model;

    /**
     * DeepSeek API密钥（加密存储）
     */
    private String deepseekApiKey;

    /**
     * GLM API密钥（加密存储）
     */
    private String glmApiKey;

    /**
     * MiniMax API密钥（加密存储）
     */
    private String minimaxApiKey;

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
}
