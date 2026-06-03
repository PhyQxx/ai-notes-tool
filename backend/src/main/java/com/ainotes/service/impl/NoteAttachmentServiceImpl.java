package com.ainotes.service.impl;

import com.ainotes.common.exception.BusinessException;
import com.ainotes.entity.NoteAttachment;
import com.ainotes.mapper.NoteAttachmentMapper;
import com.ainotes.service.NoteAttachmentService;
import com.ainotes.util.FtpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteAttachmentServiceImpl implements NoteAttachmentService {

    private final FtpUtil ftpUtil;
    private final NoteAttachmentMapper noteAttachmentMapper;

    private static final long MAX_SIZE = 50 * 1024 * 1024; // 50MB
    private static final Set<String> BLOCKED_TYPES = Set.of(
            "application/x-msdownload", "application/x-msdos-program",
            "application/x-sh", "application/x-bat", "application/octet-stream"
    );

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoteAttachment uploadAttachment(Long userId, Long noteId, MultipartFile file) {
        if (file.isEmpty()) throw new BusinessException("文件不能为空");
        if (file.getSize() > MAX_SIZE) throw new BusinessException("文件大小不能超过50MB");
        String contentType = file.getContentType();
        if (contentType != null && BLOCKED_TYPES.contains(contentType))
            throw new BusinessException("不支持的文件类型");

        try {
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (StringUtils.hasText(originalFilename)) {
                int i = originalFilename.lastIndexOf(".");
                if (i > 0) ext = originalFilename.substring(i);
            }
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String fileName = UUID.randomUUID() + ext;
            String relativePath = "attachment/" + datePath + "/" + fileName;

            byte[] data = file.getBytes();
            String url = ftpUtil.upload(data, "attachment", fileName);
            if (url == null) {
                throw new BusinessException("附件上传失败");
            }

            NoteAttachment att = new NoteAttachment();
            att.setNoteId(noteId);
            att.setFileName(originalFilename);
            att.setFilePath(relativePath);
            att.setFileSize(file.getSize());
            att.setFileType(contentType);
            att.setUploadedBy(userId);
            att.setCreatedAt(LocalDateTime.now());
            noteAttachmentMapper.insert(att);
            return att;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("附件上传失败", e);
            throw new BusinessException("附件上传失败：" + e.getMessage());
        }
    }

    @Override
    public List<NoteAttachment> listByNoteId(Long noteId) {
        return noteAttachmentMapper.selectList(
                new LambdaQueryWrapper<NoteAttachment>().eq(NoteAttachment::getNoteId, noteId)
                        .orderByDesc(NoteAttachment::getCreatedAt));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAttachment(Long userId, Long id) {
        NoteAttachment att = noteAttachmentMapper.selectById(id);
        if (att == null) throw new BusinessException("附件不存在");
        ftpUtil.delete(att.getFilePath());
        noteAttachmentMapper.deleteById(id);
    }

    @Override
    public byte[] downloadAttachment(Long id) {
        NoteAttachment att = noteAttachmentMapper.selectById(id);
        if (att == null) throw new BusinessException("附件不存在");
        byte[] data = ftpUtil.download(att.getFilePath());
        if (data == null) {
            throw new BusinessException("附件下载失败");
        }
        return data;
    }
}
