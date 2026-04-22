package com.ainotes.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ainotes.common.exception.BusinessException;
import com.ainotes.config.AIConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * MiniMax AI服务提供者
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class MiniMaxProvider implements AIProvider {

    private static final String CHAT_COMPLETIONS_URL = "/chat/completions";

    private final AIConfig aiConfig;
    private final OkHttpClient httpClient;

    private String apiKey;

    private static final List<String> SUPPORTED_MODELS = List.of(
            "MiniMax-M2.7"
    );

    public MiniMaxProvider(AIConfig aiConfig) {
        this.aiConfig = aiConfig;
        this.apiKey = aiConfig.getMinimax().getApiKey();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public String chat(String model, List<Map<String, String>> messages) {
        try {
            String url = aiConfig.getMinimax().getBaseUrl() + CHAT_COMPLETIONS_URL;

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
                    log.error("MiniMax API error: code={}, body={}", response.code(), errorBody);
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
            log.error("MiniMax API调用异常", e);
            throw new BusinessException("AI服务调用失败: " + e.getMessage());
        }
    }

    @Override
    public void chatStream(String model, List<Map<String, String>> messages, java.util.function.Consumer<String> onToken, Runnable onDone) {
        try {
            String url = aiConfig.getMinimax().getBaseUrl() + CHAT_COMPLETIONS_URL;
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("stream", true);

            RequestBody body = RequestBody.create(JSON.toJSONString(requestBody), MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    log.error("MiniMax stream error", e);
                    onDone.run();
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try (ResponseBody responseBody = response.body()) {
                        if (!response.isSuccessful()) {
                            log.error("MiniMax stream HTTP error: {}", response.code());
                            onDone.run();
                            return;
                        }
                        BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(responseBody.byteStream()));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6).trim();
                                if ("[DONE]".equals(data)) break;
                                try {
                                    JSONObject json = JSON.parseObject(data);
                                    String content = json.getJSONArray("choices")
                                            .getJSONObject(0).getJSONObject("delta").getString("content");
                                    if (content != null) onToken.accept(content);
                                } catch (Exception ignored) {}
                            }
                        }
                    } catch (Exception e) {
                        log.error("MiniMax stream read error", e);
                    } finally {
                        onDone.run();
                    }
                }
            });
        } catch (Exception e) {
            log.error("MiniMax stream init error", e);
            onDone.run();
        }
    }

    @Override
    public String getName() {
        return "minimax";
    }

    @Override
    public List<String> getSupportedModels() {
        return new ArrayList<>(SUPPORTED_MODELS);
    }

    @Override
    public String getDefaultModel() {
        return "MiniMax-M2.7";
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }
}
