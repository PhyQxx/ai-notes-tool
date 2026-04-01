package com.ainotes.controller;

import com.ainotes.dto.request.CreateFolderRequest;
import com.ainotes.entity.Folder;
import com.ainotes.service.FolderService;
import com.ainotes.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件夹控制器
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
@Validated
@Tag(name = "文件夹管理", description = "文件夹的增删改查、树形结构查询")
public class FolderController {

    private final FolderService folderService;

    /**
     * 获取文件夹列表
     *
     * @return 文件夹列表
     */
    @GetMapping
    @Operation(summary = "获取文件夹列表")
    public Result<List<Folder>> listFolders(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<Folder> folders = folderService.listFolders(userId);
        return Result.success(folders);
    }

    /**
     * 获取文件夹树
     *
     * @return 文件夹树
     */
    @GetMapping("/tree")
    @Operation(summary = "获取文件夹树")
    public Result<List<Folder>> getFolderTree(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<Folder> tree = folderService.getFolderTree(userId);
        return Result.success(tree);
    }

    /**
     * 创建文件夹
     *
     * @param request 创建请求
     * @return 文件夹ID
     */
    @PostMapping
    @Operation(summary = "创建文件夹")
    public Result<Long> createFolder(@Valid @RequestBody CreateFolderRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Long folderId = folderService.createFolder(userId, request);
        return Result.success("创建成功", folderId);
    }

    /**
     * 更新文件夹
     *
     * @param id   文件夹ID
     * @param name 文件夹名称
     * @return 成功信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新文件夹")
    public Result<Void> updateFolder(@PathVariable Long id, @RequestParam String name, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        folderService.updateFolder(userId, id, name);
        return Result.success("更新成功");
    }

    /**
     * 删除文件夹
     *
     * @param id 文件夹ID
     * @return 成功信息
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除文件夹")
    public Result<Void> deleteFolder(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        folderService.deleteFolder(userId, id);
        return Result.success("删除成功");
    }

}
