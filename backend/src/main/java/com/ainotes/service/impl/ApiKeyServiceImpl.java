package com.ainotes.service.impl;

import com.ainotes.entity.ApiKey;
import com.ainotes.mapper.ApiKeyMapper;
import com.ainotes.service.ApiKeyService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ApiKeyServiceImpl extends ServiceImpl<ApiKeyMapper, ApiKey> implements ApiKeyService {

    @Override
    public ApiKey generateKey(Long userId, String name) {
        ApiKey apiKey = new ApiKey();
        apiKey.setUserId(userId);
        apiKey.setName(name);
        apiKey.setApiKey("ak_" + UUID.randomUUID().toString().replace("-", ""));
        apiKey.setCreatedAt(LocalDateTime.now());
        apiKey.setActive(true);
        save(apiKey);
        return apiKey;
    }

    @Override
    public List<ApiKey> listKeys(Long userId) {
        return list(new LambdaQueryWrapper<ApiKey>()
                .eq(ApiKey::getUserId, userId)
                .eq(ApiKey::getActive, true));
    }

    @Override
    public void revokeKey(Long userId, Long keyId) {
        update(new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ApiKey>()
                .eq(ApiKey::getId, keyId)
                .eq(ApiKey::getUserId, userId)
                .set(ApiKey::getActive, false));
    }

    @Override
    public Long getUserIdByApiKey(String apiKey) {
        ApiKey key = getOne(new LambdaQueryWrapper<ApiKey>()
                .eq(ApiKey::getApiKey, apiKey)
                .eq(ApiKey::getActive, true));
        return key != null ? key.getUserId() : null;
    }
}
