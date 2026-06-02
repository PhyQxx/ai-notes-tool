package com.ainotes.service.impl;

import com.ainotes.common.exception.BusinessException;
import com.ainotes.dto.response.UploadResponse;
import com.ainotes.entity.Attachment;
import com.ainotes.mapper.AttachmentMapper;
import com.ainotes.service.FileService;
import io.minio.*;
import io.minio.http.Method;
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
import java.util.UUID;

/**
 * 文件服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final MinioClient minioClient;
    private final AttachmentMapper attachmentMapper;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

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
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(new java.io.ByteArrayInputStream(data), data.length, -1)
                            .contentType(contentType)
                            .build()
            );
            return endpoint + "/" + bucketName + "/" + fileName;
        } catch (Exception e) {
            log.error("字节数组上传失败", e);
            throw new BusinessException("文件上传失败");
        }
    }

    private UploadResponse uploadFileInternal(Long userId, MultipartFile file, String fileType) {
        try {
            String originalFilename = file.getOriginalFilename();
            String fileName = UUID.randomUUID().toString() + (originalFilename != null && originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "");
            String objectName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "/" + fileName;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder().method(Method.GET).bucket(bucketName).object(objectName).expiry(7 * 24 * 3600).build()
            );

            Attachment attachment = new Attachment();
            attachment.setUserId(userId);
            attachment.setFileName(originalFilename);
            attachment.setFilePath(objectName);
            attachment.setFileSize(file.getSize());
            attachment.setFileType(fileType);
            attachment.setMimeType(file.getContentType());
            attachmentMapper.insert(attachment);

            return UploadResponse.builder().url(url).fileName(originalFilename).fileSize(file.getSize()).fileType(fileType).mimeType(file.getContentType()).build();
        } catch (Exception e) {
            throw new BusinessException("上传失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long userId, Long fileId) {
        Attachment attachment = attachmentMapper.selectById(fileId);
        if (attachment != null && attachment.getUserId().equals(userId)) {
            try {
                minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(attachment.getFilePath()).build());
                attachmentMapper.deleteById(fileId);
            } catch (Exception e) {
                throw new BusinessException("删除失败");
            }
        }
    }
}
