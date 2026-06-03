package com.ainotes.service.impl;

import com.ainotes.common.exception.BusinessException;
import com.ainotes.dto.response.UploadResponse;
import com.ainotes.entity.Attachment;
import com.ainotes.mapper.AttachmentMapper;
import com.ainotes.service.FileService;
import com.ainotes.util.FtpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FtpUtil ftpUtil;
    private final AttachmentMapper attachmentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadResponse uploadImage(Long userId, MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("只能上传图片文件");
        }
        return uploadFileInternal(userId, file, "image");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadResponse uploadFile(Long userId, MultipartFile file) {
        String fileType = "other";
        String contentType = file.getContentType();
        if (contentType != null) {
            if (contentType.startsWith("image/")) fileType = "image";
            else if (contentType.startsWith("video/")) fileType = "video";
            else if (contentType.contains("pdf") || contentType.contains("word")) fileType = "document";
        }
        return uploadFileInternal(userId, file, fileType);
    }

    @Override
    public String uploadFile(byte[] data, String fileName, String contentType) {
        String url = ftpUtil.upload(data, "backups", fileName);
        if (url == null) {
            throw new BusinessException("文件上传失败");
        }
        return url;
    }

    private UploadResponse uploadFileInternal(Long userId, MultipartFile file, String fileType) {
        try {
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID() + ext;
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String relativePath = fileType + "/" + datePath + "/" + fileName;

            byte[] data = file.getBytes();
            String url = ftpUtil.upload(data, fileType, fileName);
            if (url == null) {
                throw new BusinessException("文件上传失败");
            }

            Attachment attachment = new Attachment();
            attachment.setUserId(userId);
            attachment.setFileName(originalFilename);
            attachment.setFilePath(relativePath);
            attachment.setFileSize(file.getSize());
            attachment.setFileType(fileType);
            attachment.setMimeType(file.getContentType());
            attachmentMapper.insert(attachment);

            return UploadResponse.builder()
                    .url(url)
                    .fileName(originalFilename)
                    .fileSize(file.getSize())
                    .fileType(fileType)
                    .mimeType(file.getContentType())
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("上传失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long userId, Long fileId) {
        Attachment attachment = attachmentMapper.selectById(fileId);
        if (attachment != null && attachment.getUserId().equals(userId)) {
            ftpUtil.delete(attachment.getFilePath());
            attachmentMapper.deleteById(fileId);
        }
    }
}
