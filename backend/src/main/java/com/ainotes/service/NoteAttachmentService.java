package com.ainotes.service;

import com.ainotes.entity.NoteAttachment;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface NoteAttachmentService {
    NoteAttachment uploadAttachment(Long userId, Long noteId, MultipartFile file);
    List<NoteAttachment> listByNoteId(Long noteId);
    void deleteAttachment(Long userId, Long id);
    byte[] downloadAttachment(Long id);
}
