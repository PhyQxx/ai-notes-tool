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
 *
 * @author AI Notes Team
 * @since 1.0.0
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

    /**
     * 上传图片
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadResponse uploadImage(Long userId, MultipartFile file) {
        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("只能上传图片文件");
        }

        // 校验文件大小（10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException("图片大小不能超过10MB");
        }

        return uploadFileInternal(userId, file, "image");
    }

    /**
     * 上传文件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadResponse uploadFile(Long userId, MultipartFile file) {
        // 校验文件大小（50MB）
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new BusinessException("文件大小不能超过50MB");
        }

        // 根据Content-Type判断文件类型
        String fileType = "other";
        String contentType = file.getContentType();
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                fileType = "image";
            } else if (contentType.startsWith("video/")) {
                fileType = "video";
            } else if (contentType.contains("pdf") || contentType.contains("word") ||
                    contentType.contains("excel") || contentType.contains("text")) {
                fileType = "document";
            }
        }

        return uploadFileInternal(userId, file, fileType);
    }

    /**
     * 内部文件上传方法
     */
    private UploadResponse uploadFileInternal(Long userId, MultipartFile file, String fileType) {
        try {
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (StringUtils.hasText(originalFilename)) {
                int dotIndex = originalFilename.lastIndexOf(".");
                if (dotIndex > 0) {
                    extension = originalFilename.substring(dotIndex);
                }
            }
            String fileName = UUID.randomUUID().toString() + extension;

            // 生成存储路径：日期/文件名
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String objectName = datePath + "/" + fileName;

            // 确保Bucket存在
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!found) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
            }

            // 上传文件到MinIO
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // 生成文件访问URL（有效期7天）
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(7 * 24 * 60 * 60)
                            .build()
            );

            // 保存文件记录到数据库
            Attachment attachment = new Attachment();
            attachment.setUserId(userId);
            attachment.setFileName(originalFilename);
            attachment.setFilePath(objectName);
            attachment.setFileSize(file.getSize());
            attachment.setFileType(fileType);
            attachment.setMimeType(file.getContentType());

            attachmentMapper.insert(attachment);

            log.info("文件上传成功，文件ID：{}，文件名：{}", attachment.getId(), originalFilename);

            // 构建响应
            return UploadResponse.builder()
                    .url(url)
                    .fileName(originalFilename)
                    .fileSize(file.getSize())
                    .fileType(fileType)
                    .mimeType(file.getContentType())
                    .build();

        } catch (Exception e) {
            log.error("文件上传失败：{}", e.getMessage(), e);
            throw new BusinessException("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long userId, Long fileId) {
        // 查询文件记录
        Attachment attachment = attachmentMapper.selectById(fileId);
        if (attachment == null) {
            throw new BusinessException("文件不存在");
        }

        // 校验权限
        if (!attachment.getUserId().equals(userId)) {
            throw new BusinessException("无权限删除该文件");
        }

        try {
            // 从MinIO删除文件
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(attachment.getFilePath())
                            .build()
            );

            // 删除数据库记录
            attachmentMapper.deleteById(fileId);

            log.info("文件删除成功，文件ID：{}", fileId);

        } catch (Exception e) {
            log.error("文件删除失败：{}", e.getMessage(), e);
            throw new BusinessException("文件删除失败：" + e.getMessage());
        }
    }

}
