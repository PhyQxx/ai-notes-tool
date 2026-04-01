package com.ainotes.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ainotes.common.exception.BusinessException;
import com.ainotes.config.AIConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 智谱GLM AI服务提供者
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GLMProvider implements AIProvider {

    private static final String CHAT_COMPLETIONS_URL = "/chat/completions";

    private final AIConfig aiConfig;
    private final OkHttpClient httpClient;

    private String apiKey;

    private static final List<String> SUPPORTED_MODELS = List.of(
            "glm-4",
            "glm-4-flash",
            "glm-3-turbo"
    );

    public GLMProvider(AIConfig aiConfig) {
        this.aiConfig = aiConfig;
        this.apiKey = aiConfig.getGlm().getApiKey();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public String chat(String model, List<Map<String, String>> messages) {
        try {
            String url = aiConfig.getGlm().getBaseUrl() + CHAT_COMPLETIONS_URL;

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("stream", false);

            RequestBody body = RequestBody.create(
                    JSON.toJSONString(requestBody),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    log.error("GLM API error: code={}, body={}", response.code(), errorBody);
                    throw new BusinessException("AI服务调用失败: " + errorBody);
                }

                String responseBody = response.body().string();
                JSONObject jsonResponse = JSON.parseObject(responseBody);

                if (jsonResponse.containsKey("choices") && jsonResponse.getJSONArray("choices").size() > 0) {
                    return jsonResponse.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");
                }

                throw new BusinessException("AI服务返回格式异常");
            }
        } catch (IOException e) {
            log.error("GLM API调用异常", e);
            throw new BusinessException("AI服务调用失败: " + e.getMessage());
        }
    }

    @Override
    public String chatStream(String model, List<Map<String, String>> messages) {
        // 流式对话将在后续版本中实现
        throw new BusinessException("流式对话功能暂未实现");
    }

    @Override
    public String getName() {
        return "glm";
    }

    @Override
    public List<String> getSupportedModels() {
        return new ArrayList<>(SUPPORTED_MODELS);
    }

    @Override
    public String getDefaultModel() {
        return "glm-4-flash";
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

}
