package com.ainotes.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ainotes.dto.response.VersionCompareResponse;
import com.ainotes.entity.NoteVersion;

/**
 * 笔记版本服务接口
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
public interface NoteVersionService {

    /**
     * 手动创建版本快照
     *
     * @param userId  用户ID
     * @param noteId  笔记ID
     * @param remark  版本备注
     * @return 版本ID
     */
    Long createVersion(Long userId, Long noteId, String remark);

    /**
     * 自动保存版本（笔记更新时调用）
     *
     * @param noteId 笔记ID
     */
    void autoSaveVersion(Long noteId);

    /**
     * 获取版本列表
     *
     * @param userId 用户ID
     * @param noteId 笔记ID
     * @param page   页码
     * @param size   每页大小
     * @return 版本列表
     */
    IPage<NoteVersion> listVersions(Long userId, Long noteId, Integer page, Integer size);

    /**
     * 获取版本详情
     *
     * @param userId    用户ID
     * @param noteId    笔记ID
     * @param versionId 版本ID
     * @return 版本详情
     */
    NoteVersion getVersion(Long userId, Long noteId, Long versionId);

    /**
     * 对比两个版本
     *
     * @param userId 用户ID
     * @param noteId 笔记ID
     * @param v1     版本1的ID
     * @param v2     版本2的ID
     * @return 对比结果
     */
    VersionCompareResponse compareVersions(Long userId, Long noteId, Long v1, Long v2);

    /**
     * 恢复到指定版本
     *
     * @param userId    用户ID
     * @param noteId    笔记ID
     * @param versionId 版本ID
     */
    void restoreVersion(Long userId, Long noteId, Long versionId);

    /**
     * 删除版本
     *
     * @param userId    用户ID
     * @param noteId    笔记ID
     * @param versionId 版本ID
     */
    void deleteVersion(Long userId, Long noteId, Long versionId);

}
