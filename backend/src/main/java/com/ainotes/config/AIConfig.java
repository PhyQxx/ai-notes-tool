package com.ainotes.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI配置类
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AIConfig {

    /**
     * DeepSeek配置
     */
    private DeepSeek deepseek = new DeepSeek();

    /**
     * GLM配置
     */
    private GLM glm = new GLM();

    /**
     * DeepSeek配置
     */
    @Data
    public static class DeepSeek {
        /**
         * API基础URL
         */
        private String baseUrl = "https://api.deepseek.com/v1";

        /**
         * API密钥
         */
        private String apiKey = "";

        /**
         * 默认模型
         */
        private String defaultModel = "deepseek-chat";

        /**
         * 支持的模型列表
         */
        private List<String> models = List.of("deepseek-chat", "deepseek-coder");
    }

    /**
     * GLM配置
     */
    @Data
    public static class GLM {
        /**
         * API基础URL
         */
        private String baseUrl = "https://open.bigmodel.cn/api/paas/v4";

        /**
         * API密钥
         */
        private String apiKey = "";

        /**
         * 默认模型
         */
        private String defaultModel = "glm-4-flash";

        /**
         * 支持的模型列表
         */
        private List<String> models = List.of("glm-4", "glm-4-flash", "glm-3-turbo");
    }

}
