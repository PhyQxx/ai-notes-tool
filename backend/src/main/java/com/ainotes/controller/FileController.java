package com.ainotes.controller;

import com.ainotes.dto.response.UploadResponse;
import com.ainotes.entity.Attachment;
import com.ainotes.service.FileService;
import com.ainotes.common.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件控制器
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Validated
@Tag(name = "文件管理", description = "文件上传、下载、删除等操作")
public class FileController {

    private final FileService fileService;
    private final com.ainotes.mapper.AttachmentMapper attachmentMapper;

    /**
     * 上传图片
     *
     * @param file 文件
     * @return 上传响应
     */
    @PostMapping("/upload/image")
    @Operation(summary = "上传图片")
    public Result<UploadResponse> uploadImage(@RequestParam("file") MultipartFile file, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UploadResponse response = fileService.uploadImage(userId, file);
        return Result.success("上传成功", response);
    }

    /**
     * 上传文件
     *
     * @param file 文件
     * @return 上传响应
     */
    @PostMapping("/upload/file")
    @Operation(summary = "上传文件")
    public Result<UploadResponse> uploadFile(@RequestParam("file") MultipartFile file, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UploadResponse response = fileService.uploadFile(userId, file);
        return Result.success("上传成功", response);
    }

    /**
     * 获取文件列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 文件列表
     */
    @GetMapping
    @Operation(summary = "获取文件列表")
    public Result<IPage<Attachment>> listFiles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();

        LambdaQueryWrapper<Attachment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Attachment::getUserId, userId);
        queryWrapper.orderByDesc(Attachment::getCreatedAt);

        Page<Attachment> pageParam = new Page<>(page, size);
        IPage<Attachment> result = attachmentMapper.selectPage(pageParam, queryWrapper);

        return Result.success(result);
    }

    /**
     * 删除文件
     *
     * @param id 文件ID
     * @return 成功信息
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除文件")
    public Result<Void> deleteFile(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        fileService.deleteFile(userId, id);
        return Result.success("删除成功");
    }

}
