package com.ainotes.ai;

import com.ainotes.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * AI服务提供者工厂
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AIProviderFactory {

    private final DeepSeekProvider deepSeekProvider;
    private final GLMProvider glmProvider;
    private final MiniMaxProvider miniMaxProvider;

    /**
     * 根据提供商名称获取对应的AI服务提供者
     *
     * @param providerName 提供商名称：deepseek-DeepSeek，glm-智谱GLM
     * @return AI服务提供者实例
     * @throws BusinessException 提供商不存在时抛出
     */
    public AIProvider getProvider(String providerName) {
        if ("deepseek".equalsIgnoreCase(providerName)) {
            return deepSeekProvider;
        } else if ("glm".equalsIgnoreCase(providerName)) {
            return glmProvider;
        } else if ("minimax".equalsIgnoreCase(providerName)) {
            return miniMaxProvider;
        } else {
            throw new BusinessException("不支持的AI提供商: " + providerName);
        }
    }

    /**
     * 根据提供商名称获取对应的AI服务提供者，并设置API密钥
     *
     * @param providerName 提供商名称
     * @param apiKey       API密钥
     * @return AI服务提供者实例
     * @throws BusinessException 提供商不存在时抛出
     */
    public AIProvider getProvider(String providerName, String apiKey) {
        AIProvider provider = getProvider(providerName);
        if (apiKey != null && !apiKey.isEmpty()) {
            if (provider instanceof DeepSeekProvider) {
                ((DeepSeekProvider) provider).setApiKey(apiKey);
            } else if (provider instanceof GLMProvider) {
                ((GLMProvider) provider).setApiKey(apiKey);
            }
        }
        return provider;
    }

    /**
     * 获取所有可用的提供商名称列表
     *
     * @return 提供商名称列表
     */
    public List<String> getAvailableProviders() {
        List<String> providers = new ArrayList<>();
        providers.add("deepseek");
        providers.add("glm");
        providers.add("minimax");
        return providers;
    }

    /**
     * 获取所有提供商的信息
     *
     * @return 提供商信息列表
     */
    public List<com.ainotes.dto.response.AIConfigResponse.ProviderInfo> getProviderInfos() {
        List<com.ainotes.dto.response.AIConfigResponse.ProviderInfo> infos = new ArrayList<>();

        infos.add(com.ainotes.dto.response.AIConfigResponse.ProviderInfo.builder()
                .name("deepseek")
                .displayName("DeepSeek")
                .defaultModel(deepSeekProvider.getDefaultModel())
                .models(deepSeekProvider.getSupportedModels())
                .build());

        infos.add(com.ainotes.dto.response.AIConfigResponse.ProviderInfo.builder()
                .name("glm")
                .displayName("智谱GLM")
                .defaultModel(glmProvider.getDefaultModel())
                .models(glmProvider.getSupportedModels())
                .build());

        infos.add(com.ainotes.dto.response.AIConfigResponse.ProviderInfo.builder()
                .name("minimax")
                .displayName("MiniMax")
                .defaultModel(miniMaxProvider.getDefaultModel())
                .models(miniMaxProvider.getSupportedModels())
                .build());

        return infos;
    }

}
