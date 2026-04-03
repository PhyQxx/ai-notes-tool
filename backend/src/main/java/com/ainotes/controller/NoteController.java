package com.ainotes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ainotes.dto.query.NoteQueryRequest;
import com.ainotes.dto.request.CreateNoteRequest;
import com.ainotes.dto.request.UpdateNoteRequest;
import com.ainotes.dto.response.SearchResultDTO;
import com.ainotes.entity.Note;
import com.ainotes.service.NoteService;
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
 * 笔记控制器
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
@Validated
@Tag(name = "笔记管理", description = "笔记的增删改查、搜索、收藏、置顶等操作")
public class NoteController {

    private final NoteService noteService;

    /**
     * 获取笔记列表
     *
     * @param query   查询条件
     * @return 笔记列表
     */
    @GetMapping
    @Operation(summary = "获取笔记列表")
    public Result<IPage<Note>> listNotes(NoteQueryRequest query, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        IPage<Note> page = noteService.listNotes(userId, query);
        return Result.success(page);
    }

    /**
     * 创建笔记
     *
     * @param request 创建请求
     * @return 笔记ID
     */
    @PostMapping
    @Operation(summary = "创建笔记")
    public Result<Long> createNote(@Valid @RequestBody CreateNoteRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Long noteId = noteService.createNote(userId, request);
        return Result.success("创建成功", noteId);
    }

    /**
     * 获取笔记详情
     *
     * @param id 笔记ID
     * @return 笔记详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取笔记详情")
    public Result<Note> getNoteDetail(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Note note = noteService.getNoteDetail(userId, id);
        return Result.success(note);
    }

    /**
     * 更新笔记
     *
     * @param id      笔记ID
     * @param request 更新请求
     * @return 成功信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新笔记")
    public Result<Void> updateNote(@PathVariable Long id, @Valid @RequestBody UpdateNoteRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        noteService.updateNote(userId, id, request);
        return Result.success("更新成功", null);
    }

    /**
     * 删除笔记
     *
     * @param id 笔记ID
     * @return 成功信息
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除笔记")
    public Result<Void> deleteNote(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        noteService.deleteNote(userId, id);
        return Result.success("删除成功", null);
    }

    /**
     * 搜索笔记
     *
     * @param keyword 关键词
     * @return 笔记列表
     */
    @GetMapping("/search")
    @Operation(summary = "搜索笔记")
    public Result<IPage<Note>> searchNotes(@RequestParam String keyword, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        IPage<Note> page = noteService.searchNotes(userId, keyword);
        return Result.success(page);
    }

    @GetMapping("/search/fulltext")
    @Operation(summary = "全文搜索笔记")
    public Result<IPage<SearchResultDTO>> fullTextSearch(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "all") String scope,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        IPage<SearchResultDTO> page = noteService.fullTextSearch(userId, keyword, scope);
        return Result.success(page);
    }

    /**
     * 收藏/取消收藏
     *
     * @param id 笔记ID
     * @return 成功信息
     */
    @PostMapping("/{id}/favorite")
    @Operation(summary = "收藏/取消收藏")
    public Result<Void> toggleFavorite(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        noteService.toggleFavorite(userId, id);
        return Result.success("操作成功", null);
    }

    /**
     * 置顶/取消置顶
     *
     * @param id 笔记ID
     * @return 成功信息
     */
    @PostMapping("/{id}/top")
    @Operation(summary = "置顶/取消置顶")
    public Result<Void> toggleTop(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        noteService.toggleTop(userId, id);
        return Result.success("操作成功", null);
    }

}
