package com.ainotes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ainotes.entity.Note;
import com.ainotes.entity.NoteLink;
import com.ainotes.mapper.NoteLinkMapper;
import com.ainotes.mapper.NoteMapper;
import com.ainotes.service.NoteLinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteLinkServiceImpl implements NoteLinkService {

    private final NoteLinkMapper noteLinkMapper;
    private final NoteMapper noteMapper;

    private static final Pattern LINK_PATTERN = Pattern.compile("\\[\\[(.+?)\\]\\]");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncNoteLinks(Long noteId, String content, String title) {
        // Extract [[titles]] from content
        Set<String> linkedTitles = new HashSet<>();
        if (StringUtils.hasText(content)) {
            Matcher matcher = LINK_PATTERN.matcher(content);
            while (matcher.find()) {
                linkedTitles.add(matcher.group(1).trim());
            }
        }

        // Find target note IDs by title
        Set<Long> targetNoteIds = new HashSet<>();
        for (String linkTitle : linkedTitles) {
            LambdaQueryWrapper<Note> qw = new LambdaQueryWrapper<>();
            qw.eq(Note::getTitle, linkTitle).eq(Note::getStatus, 1).last("LIMIT 1");
            Note target = noteMapper.selectOne(qw);
            if (target != null && !target.getId().equals(noteId)) {
                targetNoteIds.add(target.getId());
            }
        }

        // Get existing links
        LambdaQueryWrapper<NoteLink> existQw = new LambdaQueryWrapper<>();
        existQw.eq(NoteLink::getSourceNoteId, noteId);
        List<NoteLink> existingLinks = noteLinkMapper.selectList(existQw);
        Set<Long> existingTargetIds = new HashSet<>();
        for (NoteLink link : existingLinks) {
            existingTargetIds.add(link.getTargetNoteId());
        }

        // Delete removed links
        Set<Long> toRemove = new HashSet<>(existingTargetIds);
        toRemove.removeAll(targetNoteIds);
        if (!toRemove.isEmpty()) {
            LambdaQueryWrapper<NoteLink> delQw = new LambdaQueryWrapper<>();
            delQw.eq(NoteLink::getSourceNoteId, noteId).in(NoteLink::getTargetNoteId, toRemove);
            noteLinkMapper.delete(delQw);
        }

        // Add new links
        Set<Long> toAdd = new HashSet<>(targetNoteIds);
        toAdd.removeAll(existingTargetIds);
        for (Long targetId : toAdd) {
            Note target = noteMapper.selectById(targetId);
            if (target != null) {
                NoteLink link = new NoteLink();
                link.setSourceNoteId(noteId);
                link.setTargetNoteId(targetId);
                link.setSourceTitle(title);
                link.setTargetTitle(target.getTitle());
                noteLinkMapper.insert(link);
            }
        }
    }

    @Override
    public List<NoteLink> getBacklinks(Long noteId) {
        LambdaQueryWrapper<NoteLink> qw = new LambdaQueryWrapper<>();
        qw.eq(NoteLink::getTargetNoteId, noteId).orderByDesc(NoteLink::getCreatedAt);
        return noteLinkMapper.selectList(qw);
    }

    @Override
    public List<Note> searchTitles(Long userId, String keyword) {
        LambdaQueryWrapper<Note> qw = new LambdaQueryWrapper<>();
        qw.eq(Note::getStatus, 1);
        qw.like(Note::getTitle, keyword);
        qw.orderByDesc(Note::getUpdatedAt);
        qw.last("LIMIT 10");
        return noteMapper.selectList(qw);
    }
}
