package com.ainotes.service;

import com.ainotes.entity.ApiKey;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ApiKeyService extends IService<ApiKey> {
    ApiKey generateKey(Long userId, String name);
    List<ApiKey> listKeys(Long userId);
    void revokeKey(Long userId, Long keyId);
    Long getUserIdByApiKey(String apiKey);
}
