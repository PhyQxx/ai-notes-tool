package com.ainotes.controller;

import com.ainotes.dto.request.CreateCommentRequest;
import com.ainotes.dto.response.CommentResponse;
import com.ainotes.service.CommentService;
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
 * 评论控制器
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@Tag(name = "评论管理", description = "笔记评论的创建、查询、删除等操作")
public class CommentController {

    private final CommentService commentService;

    /**
     * 获取笔记评论列表
     *
     * @param noteId 笔记ID
     * @return 评论列表（树形结构）
     */
    @GetMapping("/notes/{noteId}/comments")
    @Operation(summary = "获取笔记评论列表")
    public Result<List<CommentResponse>> listComments(@PathVariable Long noteId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<CommentResponse> comments = commentService.listComments(userId, noteId);
        return Result.success(comments);
    }

    /**
     * 创建评论
     *
     * @param noteId  笔记ID
     * @param request 创建评论请求
     * @return 评论ID
     */
    @PostMapping("/notes/{noteId}/comments")
    @Operation(summary = "创建评论")
    public Result<Long> createComment(@PathVariable Long noteId, @Valid @RequestBody CreateCommentRequest request,
                                       Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        // 设置笔记ID
        request.setNoteId(noteId);
        Long commentId = commentService.createComment(userId, request);
        return Result.success("评论成功", commentId);
    }

    /**
     * 切换评论状态（open/resolved）
     */
    @PutMapping("/comments/{id}/status")
    @Operation(summary = "切换评论状态")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestParam String status, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        commentService.toggleStatus(userId, id, status);
        return Result.success("状态更新成功", null);
    }

    /**
     * 删除评论
     *
     * @param id 评论ID
     * @return 成功信息
     */
    @DeleteMapping("/comments/{id}")
    @Operation(summary = "删除评论")
    public Result<Void> deleteComment(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        commentService.deleteComment(userId, id);
        return Result.success("删除成功", null);
    }

}
