package com.ainotes.service;

import com.ainotes.entity.NoteShare;
import java.util.List;

public interface NoteShareService {
    NoteShare createShare(Long userId, Long noteId, String slug, String password, java.time.LocalDateTime expireAt);
    List<NoteShare> listShares(Long userId, Long noteId);
    NoteShare getBySlug(String slug);
    void deleteShare(Long userId, Long shareId);
}
