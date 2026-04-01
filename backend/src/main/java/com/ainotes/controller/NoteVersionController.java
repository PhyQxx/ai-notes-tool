package com.ainotes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ainotes.dto.request.CreateVersionRequest;
import com.ainotes.dto.response.VersionCompareResponse;
import com.ainotes.entity.NoteVersion;
import com.ainotes.service.NoteVersionService;
import com.ainotes.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 笔记版本控制器
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/notes/{noteId}/versions")
@RequiredArgsConstructor
@Validated
@Tag(name = "笔记版本管理", description = "笔记版本管理接口")
public class NoteVersionController {

    private final NoteVersionService noteVersionService;

    /**
     * 获取版本列表
     *
     * @param noteId 笔记ID
     * @param page   页码
     * @param size   每页大小
     * @return 版本列表
     */
    @GetMapping
    @Operation(summary = "获取版本列表")
    public Result<IPage<NoteVersion>> listVersions(
            @PathVariable Long noteId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        IPage<NoteVersion> pageResult = noteVersionService.listVersions(userId, noteId, page, size);
        return Result.success(pageResult);
    }

    /**
     * 获取版本详情
     *
     * @param noteId    笔记ID
     * @param versionId 版本ID
     * @return 版本详情
     */
    @GetMapping("/{versionId}")
    @Operation(summary = "获取版本详情")
    public Result<NoteVersion> getVersion(
            @PathVariable Long noteId,
            @PathVariable Long versionId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        NoteVersion version = noteVersionService.getVersion(userId, noteId, versionId);
        return Result.success(version);
    }

    /**
     * 手动创建版本快照
     *
     * @param noteId  笔记ID
     * @param request 创建请求
     * @return 版本ID
     */
    @PostMapping
    @Operation(summary = "手动创建版本快照")
    public Result<Long> createVersion(
            @PathVariable Long noteId,
            @Valid @RequestBody CreateVersionRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Long versionId = noteVersionService.createVersion(userId, noteId, request.getRemark());
        return Result.success("创建版本成功", versionId);
    }

    /**
     * 恢复版本
     *
     * @param noteId    笔记ID
     * @param versionId 版本ID
     * @return 成功信息
     */
    @PostMapping("/{versionId}/restore")
    @Operation(summary = "恢复版本")
    public Result<Void> restoreVersion(
            @PathVariable Long noteId,
            @PathVariable Long versionId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        noteVersionService.restoreVersion(userId, noteId, versionId);
        return Result.success("恢复版本成功");
    }

    /**
     * 对比版本
     *
     * @param noteId 笔记ID
     * @param v1     版本1的ID
     * @param v2     版本2的ID
     * @return 对比结果
     */
    @GetMapping("/compare")
    @Operation(summary = "对比版本")
    public Result<VersionCompareResponse> compareVersions(
            @PathVariable Long noteId,
            @RequestParam Long v1,
            @RequestParam Long v2,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        VersionCompareResponse response = noteVersionService.compareVersions(userId, noteId, v1, v2);
        return Result.success(response);
    }

    /**
     * 删除版本
     *
     * @param noteId    笔记ID
     * @param versionId 版本ID
     * @return 成功信息
     */
    @DeleteMapping("/{versionId}")
    @Operation(summary = "删除版本")
    public Result<Void> deleteVersion(
            @PathVariable Long noteId,
            @PathVariable Long versionId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        noteVersionService.deleteVersion(userId, noteId, versionId);
        return Result.success("删除版本成功");
    }

}
