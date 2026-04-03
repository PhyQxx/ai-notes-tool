package com.ainotes.controller;

import com.ainotes.common.result.Result;
import com.ainotes.entity.Note;
import com.ainotes.entity.NoteShare;
import com.ainotes.mapper.NoteMapper;
import com.ainotes.service.NoteShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "笔记分享", description = "笔记公开分享管理")
public class NoteShareController {

    private final NoteShareService noteShareService;
    private final NoteMapper noteMapper;

    @Data
    public static class CreateShareRequest {
        private String slug;
        private String password;
        private LocalDateTime expireAt;
    }

    @PostMapping("/notes/{noteId}/share")
    @Operation(summary = "创建分享")
    public Result<NoteShare> createShare(@PathVariable Long noteId,
                                          @RequestBody CreateShareRequest request,
                                          Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        if (request.getSlug() == null || request.getSlug().isBlank()) {
            request.setSlug(java.util.UUID.randomUUID().toString().substring(0, 8));
        }
        NoteShare share = noteShareService.createShare(userId, noteId, request.getSlug(), request.getPassword(), request.getExpireAt());
        return Result.success("分享创建成功", share);
    }

    @GetMapping("/notes/{noteId}/shares")
    @Operation(summary = "获取分享列表")
    public Result<List<NoteShare>> listShares(@PathVariable Long noteId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(noteShareService.listShares(userId, noteId));
    }

    @DeleteMapping("/notes/{noteId}/shares/{shareId}")
    @Operation(summary = "取消分享")
    public Result<Void> deleteShare(@PathVariable Long noteId, @PathVariable Long shareId,
                                     Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        noteShareService.deleteShare(userId, shareId);
        return Result.success("已取消分享", null);
    }

    @GetMapping("/share/{slug}")
    @Operation(summary = "获取公开分享内容")
    public Result<Map<String, Object>> getSharedNote(@PathVariable String slug,
                                                      @RequestParam(required = false) String password) {
        NoteShare share = noteShareService.getBySlug(slug);
        if (share.getPassword() != null && !share.getPassword().isBlank()) {
            if (password == null || !password.equals(share.getPassword())) {
                return Result.error("需要密码");
            }
        }
        Note note = noteMapper.selectById(share.getNoteId());
        if (note == null || note.getStatus() == 0) {
            return Result.error("笔记不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("title", note.getTitle());
        data.put("content", note.getContent());
        data.put("createdAt", note.getCreatedAt());
        data.put("author", note.getUserId());
        return Result.success(data);
    }
}
