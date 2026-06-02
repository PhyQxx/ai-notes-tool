package com.ainotes.controller;

import com.ainotes.common.result.Result;
import com.ainotes.entity.ApiKey;
import com.ainotes.service.ApiKeyService;
import com.ainotes.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/settings/api-keys")
@RequiredArgsConstructor
@Tag(name = "API Key 管理", description = "管理用户的公开 API Key")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @GetMapping
    @Operation(summary = "获取 API Key 列表")
    public Result<List<ApiKey>> listKeys() {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(apiKeyService.listKeys(userId));
    }

    @PostMapping
    @Operation(summary = "生成新的 API Key")
    public Result<ApiKey> generateKey(@RequestBody Map<String, String> body) {
        Long userId = SecurityUtil.getCurrentUserId();
        String name = body.getOrDefault("name", "默认 API Key");
        return Result.success(apiKeyService.generateKey(userId, name));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "吊销 API Key")
    public Result<Void> revokeKey(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        apiKeyService.revokeKey(userId, id);
        return Result.success("API Key 已吊销", null);
    }
}
