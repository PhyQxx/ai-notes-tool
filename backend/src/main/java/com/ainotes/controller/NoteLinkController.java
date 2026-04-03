package com.ainotes.controller;

import com.ainotes.common.result.Result;
import com.ainotes.entity.Note;
import com.ainotes.entity.NoteLink;
import com.ainotes.service.NoteLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
@Tag(name = "笔记双向链接", description = "笔记关联与反向链接")
public class NoteLinkController {

    private final NoteLinkService noteLinkService;

    @GetMapping("/{noteId}/backlinks")
    @Operation(summary = "获取反向链接")
    public Result<List<NoteLink>> getBacklinks(@PathVariable Long noteId) {
        return Result.success(noteLinkService.getBacklinks(noteId));
    }

    @GetMapping("/search-titles")
    @Operation(summary = "搜索笔记标题")
    public Result<List<Note>> searchTitles(@RequestParam String keyword, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(noteLinkService.searchTitles(userId, keyword));
    }
}
