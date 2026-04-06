package com.ainotes.service;

import com.ainotes.dto.query.NoteQueryRequest;
import com.ainotes.dto.response.SearchResultDTO;
import com.ainotes.dto.request.CreateNoteRequest;
import com.ainotes.dto.request.UpdateNoteRequest;
import com.ainotes.entity.Note;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.Map;

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
    IPage<SearchResultDTO> fullTextSearch(Long userId, String keyword, String scope);

    /**
     * 收藏/取消收藏
     *
     * @param userId 用户ID
     * @param noteId 笔记ID
     */
    void toggleFavorite(Long userId, Long noteId);

    /**
     * 取消收藏
     *
     * @param userId 用户ID
     * @param noteId 笔记ID
     */
    void unfavorite(Long userId, Long noteId);

    /**
     * 置顶/取消置顶
     *
     * @param userId 用户ID
     * @param noteId 笔记ID
     */
    void toggleTop(Long userId, Long noteId);

    /**
     * 取消置顶
     *
     * @param userId 用户ID
     * @param noteId 笔记ID
     */
    void untop(Long userId, Long noteId);

    /**
     * 获取回收站列表
     */
    IPage<Note> listTrash(Long userId, Integer page, Integer size);

    /**
     * 从回收站恢复笔记
     */
    void restoreNote(Long userId, Long noteId);

    /**
     * 彻底删除笔记
     */
    void permanentDeleteNote(Long userId, Long noteId);

    /**
     * 清空回收站
     */
    void emptyTrash(Long userId);

    /**
     * 获取最近编辑的笔记
     */
    List<Note> recentNotes(Long userId, Integer limit);

    /**
     * 获取收藏的笔记（分页）
     */
    IPage<Note> favoriteNotes(Long userId, Integer page, Integer size);

    /**
     * 获取推荐笔记（基于标签相似度）
     */
    List<Note> recommendNotes(Long userId, Long noteId, Integer limit);

    /**
     * 获取知识图谱数据
     */
    Map<String, Object> getGraphData(Long userId);

}
