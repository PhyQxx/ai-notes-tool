package com.ainotes.common.enums;

import lombok.Getter;

/**
 * AI提供商枚举
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Getter
public enum AIProvider {

    /**
     * DeepSeek
     */
    DEEPSEEK("deepseek", "DeepSeek"),

    /**
     * 智谱GLM
     */
    GLM("glm", "智谱GLM"),

    /**
     * MiniMax
     */
    MINIMAX("minimax", "MiniMax");

    /**
     * 提供商编码
     */
    private final String code;

    /**
     * 提供商名称
     */
    private final String name;

    /**
     * 构造方法
     *
     * @param code 提供商编码
     * @param name 提供商名称
     */
    AIProvider(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 提供商编码
     * @return 枚举对象
     */
    public static AIProvider fromCode(String code) {
        for (AIProvider provider : values()) {
            if (provider.getCode().equals(code)) {
                return provider;
            }
        }
        return DEEPSEEK; // 默认返回DeepSeek
    }

}
