package com.ainotes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ainotes.common.exception.BusinessException;
import com.ainotes.dto.query.NoteQueryRequest;
import com.ainotes.dto.request.CreateNoteRequest;
import com.ainotes.dto.request.UpdateNoteRequest;
import com.ainotes.entity.Note;
import com.ainotes.mapper.NoteMapper;
import com.ainotes.service.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 笔记服务实现类
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteMapper noteMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createNote(Long userId, CreateNoteRequest request) {
        Note note = new Note();
        note.setUserId(userId);
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        // 默认使用markdown
        note.setContentType(StringUtils.hasText(request.getContentType()) ? request.getContentType() : "markdown");
        note.setFolderId(request.getFolderId());
        note.setTags(request.getTags());
        note.setIsFavorite(0);
        note.setIsTop(0);
        note.setViewCount(0);
        note.setStatus(1);

        noteMapper.insert(note);
        log.info("创建笔记成功，笔记ID：{}", note.getId());

        return note.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNote(Long userId, Long noteId, UpdateNoteRequest request) {
        // 查询笔记
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }

        // 校验权限
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作该笔记");
        }

        // 校验状态
        if (note.getStatus() == 0) {
            throw new BusinessException("笔记已被删除");
        }

        // 更新字段
        if (StringUtils.hasText(request.getTitle())) {
            note.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            note.setContent(request.getContent());
        }
        if (StringUtils.hasText(request.getContentType())) {
            note.setContentType(request.getContentType());
        }
        if (request.getFolderId() != null) {
            note.setFolderId(request.getFolderId());
        }
        if (request.getTags() != null) {
            note.setTags(request.getTags());
        }

        noteMapper.updateById(note);
        log.info("更新笔记成功，笔记ID：{}", noteId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNote(Long userId, Long noteId) {
        // 查询笔记
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }

        // 校验权限
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作该笔记");
        }

        // 软删除
        note.setStatus(0);
        noteMapper.updateById(note);
        log.info("删除笔记成功，笔记ID：{}", noteId);
    }

    @Override
    public Note getNoteDetail(Long userId, Long noteId) {
        // 查询笔记
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }

        // 校验权限
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权限查看该笔记");
        }

        // 校验状态
        if (note.getStatus() == 0) {
            throw new BusinessException("笔记已被删除");
        }

        // 增加查看次数
        note.setViewCount(note.getViewCount() + 1);
        noteMapper.updateById(note);

        return note;
    }

    @Override
    public IPage<Note> listNotes(Long userId, NoteQueryRequest query) {
        // 构建查询条件
        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getUserId, userId);
        queryWrapper.eq(Note::getStatus, 1);

        // 文件夹筛选
        if (query.getFolderId() != null) {
            queryWrapper.eq(Note::getFolderId, query.getFolderId());
        }

        // 标签筛选
        if (StringUtils.hasText(query.getTag())) {
            queryWrapper.like(Note::getTags, query.getTag());
        }

        // 收藏筛选
        if (query.getIsFavorite() != null) {
            queryWrapper.eq(Note::getIsFavorite, query.getIsFavorite());
        }

        // 置顶筛选
        if (query.getIsTop() != null) {
            queryWrapper.eq(Note::getIsTop, query.getIsTop());
        }

        // 排序：置顶优先，然后按更新时间倒序
        queryWrapper.orderByDesc(Note::getIsTop);
        queryWrapper.orderByDesc(Note::getUpdatedAt);

        // 分页查询
        Page<Note> page = new Page<>(query.getPage(), query.getSize());
        return noteMapper.selectPage(page, queryWrapper);
    }

    @Override
    public IPage<Note> searchNotes(Long userId, String keyword) {
        // 构建查询条件
        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getUserId, userId);
        queryWrapper.eq(Note::getStatus, 1);

        // 关键词搜索（标题或内容）
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Note::getTitle, keyword)
                    .or()
                    .like(Note::getContent, keyword)
            );
        }

        // 排序：按更新时间倒序
        queryWrapper.orderByDesc(Note::getUpdatedAt);

        // 分页查询（默认每页20条）
        Page<Note> page = new Page<>(1, 20);
        return noteMapper.selectPage(page, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleFavorite(Long userId, Long noteId) {
        // 查询笔记
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }

        // 校验权限
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作该笔记");
        }

        // 校验状态
        if (note.getStatus() == 0) {
            throw new BusinessException("笔记已被删除");
        }

        // 切换收藏状态
        note.setIsFavorite(note.getIsFavorite() == 1 ? 0 : 1);
        noteMapper.updateById(note);
        log.info("切换笔记收藏状态，笔记ID：{}，收藏状态：{}", noteId, note.getIsFavorite());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleTop(Long userId, Long noteId) {
        // 查询笔记
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }

        // 校验权限
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作该笔记");
        }

        // 校验状态
        if (note.getStatus() == 0) {
            throw new BusinessException("笔记已被删除");
        }

        // 切换置顶状态
        note.setIsTop(note.getIsTop() == 1 ? 0 : 1);
        noteMapper.updateById(note);
        log.info("切换笔记置顶状态，笔记ID：{}，置顶状态：{}", noteId, note.getIsTop());
    }

}
