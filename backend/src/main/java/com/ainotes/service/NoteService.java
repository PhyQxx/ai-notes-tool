package com.ainotes.service;

import com.ainotes.dto.query.NoteQueryRequest;
import com.ainotes.dto.request.CreateNoteRequest;
import com.ainotes.dto.request.UpdateNoteRequest;
import com.ainotes.entity.Note;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 笔记服务接口
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
public interface NoteService {

    /**
     * 创建笔记
     *
     * @param userId  用户ID
     * @param request 创建请求
     * @return 笔记ID
     */
    Long createNote(Long userId, CreateNoteRequest request);

    /**
     * 更新笔记
     *
     * @param userId  用户ID
     * @param noteId  笔记ID
     * @param request 更新请求
     */
    void updateNote(Long userId, Long noteId, UpdateNoteRequest request);

    /**
     * 删除笔记（软删除）
     *
     * @param userId 用户ID
     * @param noteId 笔记ID
     */
    void deleteNote(Long userId, Long noteId);

    /**
     * 获取笔记详情
     *
     * @param userId 用户ID
     * @param noteId 笔记ID
     * @return 笔记信息
     */
    Note getNoteDetail(Long userId, Long noteId);

    /**
     * 笔记列表（分页查询）
     *
     * @param userId 用户ID
     * @param query  查询条件
     * @return 分页结果
     */
    IPage<Note> listNotes(Long userId, NoteQueryRequest query);

    /**
     * 搜索笔记
     *
     * @param userId  用户ID
     * @param keyword 关键词
     * @return 笔记列表
     */
    IPage<Note> searchNotes(Long userId, String keyword);

    /**
     * 收藏/取消收藏
     *
     * @param userId 用户ID
     * @param noteId 笔记ID
     */
    void toggleFavorite(Long userId, Long noteId);

    /**
     * 置顶/取消置顶
     *
     * @param userId 用户ID
     * @param noteId 笔记ID
     */
    void toggleTop(Long userId, Long noteId);

}
