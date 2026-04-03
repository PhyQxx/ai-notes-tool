package com.ainotes.service.impl;

import com.ainotes.common.exception.BusinessException;
import com.ainotes.entity.NoteAttachment;
import com.ainotes.mapper.NoteAttachmentMapper;
import com.ainotes.service.NoteAttachmentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteAttachmentServiceImpl implements NoteAttachmentService {

    private final MinioClient minioClient;
    private final NoteAttachmentMapper noteAttachmentMapper;

    @Value("${minio.bucket-name:ai-notes}")
    private String defaultBucket;
    private static final String BUCKET = "notes-attachments";

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
            String objectName = datePath + "/" + UUID.randomUUID() + ext;

            ensureBucket();
            try (InputStream is = file.getInputStream()) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(BUCKET).object(objectName)
                        .stream(is, file.getSize(), -1)
                        .contentType(contentType).build());
            }

            NoteAttachment att = new NoteAttachment();
            att.setNoteId(noteId);
            att.setFileName(originalFilename);
            att.setFilePath(objectName);
            att.setFileSize(file.getSize());
            att.setFileType(contentType);
            att.setUploadedBy(userId);
            att.setCreatedAt(LocalDateTime.now());
            noteAttachmentMapper.insert(att);
            return att;
        } catch (BusinessException e) { throw e; }
        catch (Exception e) {
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
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(BUCKET).object(att.getFilePath()).build());
            noteAttachmentMapper.deleteById(id);
        } catch (Exception e) {
            log.error("附件删除失败", e);
            throw new BusinessException("附件删除失败：" + e.getMessage());
        }
    }

    @Override
    public byte[] downloadAttachment(Long id) {
        NoteAttachment att = noteAttachmentMapper.selectById(id);
        if (att == null) throw new BusinessException("附件不存在");
        try {
            try (InputStream is = minioClient.getObject(GetObjectArgs.builder().bucket(BUCKET).object(att.getFilePath()).build())) {
                return is.readAllBytes();
            }
        } catch (Exception e) {
            log.error("附件下载失败", e);
            throw new BusinessException("附件下载失败：" + e.getMessage());
        }
    }

    private void ensureBucket() {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET).build());
            }
        } catch (Exception e) {
            log.warn("检查bucket失败", e);
        }
    }
}
