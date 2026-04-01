package com.ainotes.controller;

import com.ainotes.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 导出控制器
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/notes/{noteId}/export")
@RequiredArgsConstructor
@Validated
@Tag(name = "笔记导出", description = "笔记导出接口")
public class ExportController {

    private final ExportService exportService;

    /**
     * 导出Markdown文件
     *
     * @param noteId 笔记ID
     * @return 文件响应
     */
    @PostMapping("/md")
    @Operation(summary = "导出Markdown文件")
    public ResponseEntity<byte[]> exportMarkdown(
            @PathVariable Long noteId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return exportService.exportMarkdown(userId, noteId);
    }

    /**
     * 导出PDF文件
     *
     * @param noteId 笔记ID
     * @return 文件响应
     */
    @PostMapping("/pdf")
    @Operation(summary = "导出PDF文件")
    public ResponseEntity<byte[]> exportPDF(
            @PathVariable Long noteId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return exportService.exportPDF(userId, noteId);
    }

    /**
     * 导出Word文件
     *
     * @param noteId 笔记ID
     * @return 文件响应
     */
    @PostMapping("/word")
    @Operation(summary = "导出Word文件")
    public ResponseEntity<byte[]> exportWord(
            @PathVariable Long noteId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return exportService.exportWord(userId, noteId);
    }

}
