package com.ainotes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ainotes.common.exception.BusinessException;
import com.ainotes.entity.Note;
import com.ainotes.entity.NoteShare;
import com.ainotes.mapper.NoteMapper;
import com.ainotes.mapper.NoteShareMapper;
import com.ainotes.service.NoteShareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteShareServiceImpl implements NoteShareService {

    private final NoteShareMapper noteShareMapper;
    private final NoteMapper noteMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoteShare createShare(Long userId, Long noteId, String slug, String password, LocalDateTime expireAt) {
        Note note = noteMapper.selectById(noteId);
        if (note == null || note.getStatus() == 0) {
            throw new BusinessException("笔记不存在");
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("只能分享自己的笔记");
        }

        // Check slug uniqueness
        LambdaQueryWrapper<NoteShare> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteShare::getSlug, slug);
        if (noteShareMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("分享链接已存在，请更换slug");
        }

        NoteShare share = new NoteShare();
        share.setNoteId(noteId);
        share.setSlug(slug);
        share.setPassword(password);
        share.setExpireAt(expireAt);
        share.setViewCount(0);
        share.setCreatedBy(userId);
        noteShareMapper.insert(share);
        return share;
    }

    @Override
    public List<NoteShare> listShares(Long userId, Long noteId) {
        LambdaQueryWrapper<NoteShare> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteShare::getNoteId, noteId);
        wrapper.eq(NoteShare::getCreatedBy, userId);
        wrapper.orderByDesc(NoteShare::getCreatedAt);
        return noteShareMapper.selectList(wrapper);
    }

    @Override
    public NoteShare getBySlug(String slug) {
        LambdaQueryWrapper<NoteShare> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteShare::getSlug, slug);
        NoteShare share = noteShareMapper.selectOne(wrapper);
        if (share == null) {
            throw new BusinessException("分享不存在");
        }
        if (share.getExpireAt() != null && share.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("分享已过期");
        }
        // Increment view count
        share.setViewCount(share.getViewCount() + 1);
        noteShareMapper.updateById(share);
        return share;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteShare(Long userId, Long shareId) {
        NoteShare share = noteShareMapper.selectById(shareId);
        if (share == null) {
            throw new BusinessException("分享不存在");
        }
        if (!share.getCreatedBy().equals(userId)) {
            throw new BusinessException("无权删除");
        }
        noteShareMapper.deleteById(shareId);
    }
}
