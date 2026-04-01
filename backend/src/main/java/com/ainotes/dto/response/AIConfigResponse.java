package com.ainotes.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI配置响应DTO
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIConfigResponse {

    /**
     * 当前AI提供商
     */
    private String provider;

    /**
     * 当前AI模型
     */
    private String model;

    /**
     * 可用的提供商列表
     */
    private List<ProviderInfo> providers;

    /**
     * 当前提供商的可用模型列表
     */
    private List<String> models;

    /**
     * 提供商信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProviderInfo {
        /**
         * 提供商名称
         */
        private String name;

        /**
         * 提供商显示名称
         */
        private String displayName;

        /**
         * 默认模型
         */
        private String defaultModel;

        /**
         * 所有可用模型
         */
        private List<String> models;
    }

}
