package com.ainotes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ainotes.common.exception.BusinessException;
import com.ainotes.dto.request.CreateFolderRequest;
import com.ainotes.entity.Folder;
import com.ainotes.mapper.FolderMapper;
import com.ainotes.mapper.NoteMapper;
import com.ainotes.service.FolderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件夹服务实现类
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

    private final FolderMapper folderMapper;
    private final NoteMapper noteMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFolder(Long userId, CreateFolderRequest request) {
        // 如果指定了父文件夹，验证父文件夹是否存在
        if (request.getParentId() != null && !request.getParentId().equals(0L)) {
            Folder parentFolder = folderMapper.selectById(request.getParentId());
            if (parentFolder == null) {
                throw new BusinessException("父文件夹不存在");
            }
            if (!parentFolder.getUserId().equals(userId)) {
                throw new BusinessException("无权限在该文件夹下创建");
            }
        }

        // 创建文件夹
        Folder folder = new Folder();
        folder.setUserId(userId);
        folder.setName(request.getName());
        folder.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        folder.setSortOrder(0);

        folderMapper.insert(folder);
        log.info("创建文件夹成功，文件夹ID：{}", folder.getId());

        return folder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFolder(Long userId, Long folderId, String name) {
        // 查询文件夹
        Folder folder = folderMapper.selectById(folderId);
        if (folder == null) {
            throw new BusinessException("文件夹不存在");
        }

        // 校验权限
        if (!folder.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作该文件夹");
        }

        // 更新名称
        folder.setName(name);
        folderMapper.updateById(folder);
        log.info("更新文件夹成功，文件夹ID：{}", folderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFolder(Long userId, Long folderId) {
        // 查询文件夹
        Folder folder = folderMapper.selectById(folderId);
        if (folder == null) {
            throw new BusinessException("文件夹不存在");
        }

        // 校验权限
        if (!folder.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作该文件夹");
        }

        // 检查是否有子文件夹
        LambdaQueryWrapper<Folder> childQuery = new LambdaQueryWrapper<>();
        childQuery.eq(Folder::getParentId, folderId);
        long childCount = folderMapper.selectCount(childQuery);
        if (childCount > 0) {
            throw new BusinessException("文件夹下有子文件夹，无法删除");
        }

        // 检查是否有笔记
        LambdaQueryWrapper<com.ainotes.entity.Note> noteQuery = new LambdaQueryWrapper<>();
        noteQuery.eq(com.ainotes.entity.Note::getFolderId, folderId);
        noteQuery.eq(com.ainotes.entity.Note::getStatus, 1);
        long noteCount = noteMapper.selectCount(noteQuery);
        if (noteCount > 0) {
            throw new BusinessException("文件夹下有笔记，无法删除");
        }

        // 删除文件夹
        folderMapper.deleteById(folderId);
        log.info("删除文件夹成功，文件夹ID：{}", folderId);
    }

    @Override
    public List<Folder> listFolders(Long userId) {
        // 查询所有文件夹
        LambdaQueryWrapper<Folder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Folder::getUserId, userId);
        queryWrapper.orderByAsc(Folder::getSortOrder);
        queryWrapper.orderByDesc(Folder::getCreatedAt);

        return folderMapper.selectList(queryWrapper);
    }

    @Override
    public List<Folder> getFolderTree(Long userId) {
        // 查询所有文件夹
        List<Folder> allFolders = listFolders(userId);

        // 构建树形结构
        return buildTree(allFolders, 0L);
    }

    /**
     * 递归构建文件夹树
     *
     * @param allFolders 所有文件夹
     * @param parentId   父文件夹ID
     * @return 树形结构
     */
    private List<Folder> buildTree(List<Folder> allFolders, Long parentId) {
        List<Folder> tree = new ArrayList<>();

        for (Folder folder : allFolders) {
            if (folder.getParentId().equals(parentId)) {
                // 递归构建子文件夹
                List<Folder> children = buildTree(allFolders, folder.getId());
                folder.setChildren(children);
                tree.add(folder);
            }
        }

        return tree;
    }

}
