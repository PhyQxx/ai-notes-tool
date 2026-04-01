package com.ainotes.service;

import com.ainotes.dto.request.CreateFolderRequest;
import com.ainotes.entity.Folder;

import java.util.List;

/**
 * 文件夹服务接口
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
public interface FolderService {

    /**
     * 创建文件夹
     *
     * @param userId  用户ID
     * @param request 创建请求
     * @return 文件夹ID
     */
    Long createFolder(Long userId, CreateFolderRequest request);

    /**
     * 更新文件夹
     *
     * @param userId   用户ID
     * @param folderId 文件夹ID
     * @param name     文件夹名称
     */
    void updateFolder(Long userId, Long folderId, String name);

    /**
     * 删除文件夹
     *
     * @param userId   用户ID
     * @param folderId 文件夹ID
     */
    void deleteFolder(Long userId, Long folderId);

    /**
     * 获取文件夹列表
     *
     * @param userId 用户ID
     * @return 文件夹列表
     */
    List<Folder> listFolders(Long userId);

    /**
     * 获取文件夹树
     *
     * @param userId 用户ID
     * @return 文件夹树
     */
    List<Folder> getFolderTree(Long userId);

}
