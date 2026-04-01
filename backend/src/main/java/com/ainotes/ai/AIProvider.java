package com.ainotes.ai;

import com.ainotes.common.exception.BusinessException;

import java.util.List;
import java.util.Map;

/**
 * AI服务提供者接口
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
public interface AIProvider {

    /**
     * 同步对话
     *
     * @param model    模型名称
     * @param messages 消息列表
     * @return AI回复内容
     * @throws BusinessException 调用失败时抛出
     */
    String chat(String model, List<Map<String, String>> messages);

    /**
     * 流式对话（返回SSE）
     *
     * @param model    模型名称
     * @param messages 消息列表
     * @return 流式响应内容
     * @throws BusinessException 调用失败时抛出
     */
    String chatStream(String model, List<Map<String, String>> messages);

    /**
     * 获取提供商名称
     *
     * @return 提供商名称
     */
    String getName();

    /**
     * 获取支持的模型列表
     *
     * @return 模型列表
     */
    List<String> getSupportedModels();

    /**
     * 获取默认模型
     *
     * @return 默认模型名称
     */
    String getDefaultModel();

}
