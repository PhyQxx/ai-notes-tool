package com.ainotes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ainotes.common.exception.BusinessException;
import com.ainotes.dto.response.VersionCompareResponse;
import com.ainotes.entity.Note;
import com.ainotes.entity.NoteVersion;
import com.ainotes.mapper.NoteMapper;
import com.ainotes.mapper.NoteVersionMapper;
import com.ainotes.service.NoteVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 笔记版本服务实现类
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteVersionServiceImpl implements NoteVersionService {

    private final NoteVersionMapper noteVersionMapper;
    private final NoteMapper noteMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createVersion(Long userId, Long noteId, String remark) {
        // 查询笔记
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }

        // 校验权限
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作该笔记");
        }

        // 校验状态
        if (note.getStatus() == 0) {
            throw new BusinessException("笔记已被删除");
        }

        // 获取下一个版本号
        LambdaQueryWrapper<NoteVersion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoteVersion::getNoteId, noteId);
        Integer maxVersionNo = noteVersionMapper.selectList(queryWrapper)
                .stream()
                .map(NoteVersion::getVersionNo)
                .max(Integer::compareTo)
                .orElse(0);
        Integer nextVersionNo = maxVersionNo + 1;

        // 创建版本
        NoteVersion version = new NoteVersion();
        version.setNoteId(noteId);
        version.setVersionNo(nextVersionNo);
        version.setTitle(note.getTitle());
        version.setContent(note.getContent());
        version.setRemark(remark);
        version.setCreatedBy(userId);

        noteVersionMapper.insert(version);
        log.info("创建笔记版本成功，笔记ID：{}，版本号：{}", noteId, nextVersionNo);

        return version.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoSaveVersion(Long noteId) {
        // 查询笔记
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            log.warn("笔记不存在，自动保存版本失败，笔记ID：{}", noteId);
            return;
        }

        // 获取最后一个版本
        LambdaQueryWrapper<NoteVersion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoteVersion::getNoteId, noteId);
        queryWrapper.orderByDesc(NoteVersion::getVersionNo);
        NoteVersion lastVersion = noteVersionMapper.selectOne(queryWrapper);

        // 如果没有版本，或者内容变化超过5%，则自动保存
        if (lastVersion == null || shouldAutoSave(note.getContent(), lastVersion.getContent())) {
            // 获取下一个版本号
            Integer maxVersionNo = noteVersionMapper.selectList(
                            new LambdaQueryWrapper<NoteVersion>().eq(NoteVersion::getNoteId, noteId))
                    .stream()
                    .map(NoteVersion::getVersionNo)
                    .max(Integer::compareTo)
                    .orElse(0);
            Integer nextVersionNo = maxVersionNo + 1;

            // 创建自动保存的版本
            NoteVersion version = new NoteVersion();
            version.setNoteId(noteId);
            version.setVersionNo(nextVersionNo);
            version.setTitle(note.getTitle());
            version.setContent(note.getContent());
            version.setRemark("自动保存");
            version.setCreatedBy(note.getUserId());

            noteVersionMapper.insert(version);
            log.info("自动保存笔记版本成功，笔记ID：{}，版本号：{}", noteId, nextVersionNo);
        }
    }

    /**
     * 判断是否应该自动保存（内容变化超过5%）
     *
     * @param newContent    新内容
     * @param oldContent    旧内容
     * @return true-应该保存，false-不应该保存
     */
    private boolean shouldAutoSave(String newContent, String oldContent) {
        if (newContent == null || oldContent == null) {
            return true;
        }

        int newLength = newContent.length();
        int oldLength = oldContent.length();

        // 如果内容为空，不保存
        if (newLength == 0 && oldLength == 0) {
            return false;
        }

        // 计算长度差异
        int diff = Math.abs(newLength - oldLength);
        int maxLength = Math.max(newLength, oldLength);

        // 如果最大长度为0，不保存
        if (maxLength == 0) {
            return false;
        }

        // 计算变化比例
        double changeRate = (double) diff / maxLength;

        // 变化超过5%，则保存
        return changeRate > 0.05;
    }

    @Override
    public IPage<NoteVersion> listVersions(Long userId, Long noteId, Integer page, Integer size) {
        // 查询笔记
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }

        // 校验权限
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权限查看该笔记");
        }

        // 构建查询条件
        LambdaQueryWrapper<NoteVersion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoteVersion::getNoteId, noteId);
        queryWrapper.orderByDesc(NoteVersion::getVersionNo);

        // 分页查询
        Page<NoteVersion> pageObj = new Page<>(page, size);
        return noteVersionMapper.selectPage(pageObj, queryWrapper);
    }

    @Override
    public NoteVersion getVersion(Long userId, Long noteId, Long versionId) {
        // 查询笔记
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }

        // 校验权限
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权限查看该笔记");
        }

        // 查询版本
        NoteVersion version = noteVersionMapper.selectById(versionId);
        if (version == null) {
            throw new BusinessException("版本不存在");
        }

        // 校验版本是否属于该笔记
        if (!version.getNoteId().equals(noteId)) {
            throw new BusinessException("版本不属于该笔记");
        }

        return version;
    }

    @Override
    public VersionCompareResponse compareVersions(Long userId, Long noteId, Long v1, Long v2) {
        // 查询两个版本
        NoteVersion version1 = getVersion(userId, noteId, v1);
        NoteVersion version2 = getVersion(userId, noteId, v2);

        // 构建响应
        VersionCompareResponse response = new VersionCompareResponse();

        VersionCompareResponse.VersionInfo vi1 = new VersionCompareResponse.VersionInfo();
        vi1.setId(version1.getId());
        vi1.setVersionNo(version1.getVersionNo());
        vi1.setTitle(version1.getTitle());
        vi1.setContent(version1.getContent());
        vi1.setRemark(version1.getRemark());
        vi1.setCreatedAt(version1.getCreatedAt().toString());
        vi1.setCreatedBy(version1.getCreatedBy());

        VersionCompareResponse.VersionInfo vi2 = new VersionCompareResponse.VersionInfo();
        vi2.setId(version2.getId());
        vi2.setVersionNo(version2.getVersionNo());
        vi2.setTitle(version2.getTitle());
        vi2.setContent(version2.getContent());
        vi2.setRemark(version2.getRemark());
        vi2.setCreatedAt(version2.getCreatedAt().toString());
        vi2.setCreatedBy(version2.getCreatedBy());

        response.setVersion1(vi1);
        response.setVersion2(vi2);

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreVersion(Long userId, Long noteId, Long versionId) {
        // 查询笔记
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }

        // 校验权限
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作该笔记");
        }

        // 查询版本
        NoteVersion version = getVersion(userId, noteId, versionId);

        // 恢复版本
        note.setTitle(version.getTitle());
        note.setContent(version.getContent());
        noteMapper.updateById(note);

        log.info("恢复笔记版本成功，笔记ID：{}，版本号：{}", noteId, version.getVersionNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVersion(Long userId, Long noteId, Long versionId) {
        // 查询笔记
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }

        // 校验权限
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作该笔记");
        }

        // 查询版本
        NoteVersion version = getVersion(userId, noteId, versionId);

        // 删除版本
        noteVersionMapper.deleteById(versionId);
        log.info("删除笔记版本成功，笔记ID：{}，版本号：{}", noteId, version.getVersionNo());
    }

}
