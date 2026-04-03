package com.ainotes.service;

import com.ainotes.entity.Note;
import com.ainotes.entity.NoteLink;

import java.util.List;

public interface NoteLinkService {

    /**
     * 解析笔记内容中的 [[标题]] 链接并同步到数据库
     */
    void syncNoteLinks(Long noteId, String content, String title);

    /**
     * 获取反向链接列表
     */
    List<NoteLink> getBacklinks(Long noteId);

    /**
     * 搜索笔记标题（用于 [[ 选择器）
     */
    List<Note> searchTitles(Long userId, String keyword);
}
