package com.ainotes.service;

import com.ainotes.dto.response.UploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
public interface FileService {

    /**
     * 上传图片
     *
     * @param userId 用户ID
     * @param file   文件
     * @return 上传响应
     */
    UploadResponse uploadImage(Long userId, MultipartFile file);

    /**
     * 上传文件
     *
     * @param userId 用户ID
     * @param file   文件
     * @return 上传响应
     */
    UploadResponse uploadFile(Long userId, MultipartFile file);

    /**
     * 删除文件
     *
     * @param userId 用户ID
     * @param fileId 文件ID
     */
    void deleteFile(Long userId, Long fileId);

}
