package com.ainotes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ainotes.common.exception.BusinessException;
import com.ainotes.dto.query.NoteQueryRequest;
import com.ainotes.dto.request.CreateNoteRequest;
import com.ainotes.dto.request.UpdateNoteRequest;
import com.ainotes.entity.Note;
import com.ainotes.entity.SpaceMember;
import com.ainotes.mapper.NoteMapper;
import com.ainotes.mapper.SpaceMemberMapper;
import com.ainotes.service.NoteService;
import com.ainotes.service.NoteVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 笔记服务实现类
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteMapper noteMapper;
    private final NoteVersionService noteVersionService;
    private final SpaceMemberMapper spaceMemberMapper;

    /**
     * 角色权限等级
     */
    private static final int ROLE_LEVEL_OWNER = 100;
    private static final int ROLE_LEVEL_ADMIN = 80;
    private static final int ROLE_LEVEL_EDITOR = 60;
    private static final int ROLE_LEVEL_VIEWER = 40;

    /**
     * 记录每个笔记的上次自动保存时间
     */
    private final Map<Long, LocalDateTime> lastAutoSaveTimeMap = new ConcurrentHashMap<>();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createNote(Long userId, CreateNoteRequest request) {
        Note note = new Note();
        note.setUserId(userId);
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        // 默认使用markdown
        note.setContentType(StringUtils.hasText(request.getContentType()) ? request.getContentType() : "markdown");
        note.setFolderId(request.getFolderId());
        note.setTags(request.getTags());
        note.setIsFavorite(0);
        note.setIsTop(0);
        note.setViewCount(0);
        note.setStatus(1);

        // 如果指定了空间ID，检查权限
        if (request.getSpaceId() != null && request.getSpaceId() > 0) {
            // 检查用户是否有该空间的编辑权限
            checkSpacePermission(userId, request.getSpaceId(), "editor");
            note.setSpaceId(request.getSpaceId());
        }

        noteMapper.insert(note);
        log.info("创建笔记成功，笔记ID：{}", note.getId());

        return note.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNote(Long userId, Long noteId, UpdateNoteRequest request) {
        // 查询笔记
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }

        // 校验状态
        if (note.getStatus() == 0) {
            throw new BusinessException("笔记已被删除");
        }

        // 检查权限
        checkNotePermission(userId, note);

        // 保存旧内容，用于判断是否需要自动保存版本
        String oldContent = note.getContent();
        int oldLength = oldContent != null ? oldContent.length() : 0;

        // 更新字段
        if (StringUtils.hasText(request.getTitle())) {
            note.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            note.setContent(request.getContent());
        }
        if (StringUtils.hasText(request.getContentType())) {
            note.setContentType(request.getContentType());
        }
        if (request.getFolderId() != null) {
            note.setFolderId(request.getFolderId());
        }
        if (request.getTags() != null) {
            note.setTags(request.getTags());
        }

        // 更新笔记
        noteMapper.updateById(note);

        // 检查是否需要自动保存版本
        checkAndAutoSaveVersion(noteId, oldContent);

        log.info("更新笔记成功，笔记ID：{}", noteId);
    }

    /**
     * 检查并自动保存版本
     *
     * @param noteId     笔记ID
     * @param oldContent 旧内容
     */
    private void checkAndAutoSaveVersion(Long noteId, String oldContent) {
        // 获取当前笔记
        Note note = noteMapper.selectById(noteId);
        if (note == null || note.getContent() == null) {
            return;
        }

        String newContent = note.getContent();
        int newLength = newContent.length();
        int oldLength = oldContent != null ? oldContent.length() : 0;

        // 获取上次自动保存时间
        LocalDateTime lastAutoSaveTime = lastAutoSaveTimeMap.get(noteId);
        LocalDateTime now = LocalDateTime.now();

        // 计算时间差（分钟）
        long minutesSinceLastSave = lastAutoSaveTime != null ?
                java.time.Duration.between(lastAutoSaveTime, now).toMinutes() : Long.MAX_VALUE;

        // 计算内容变化比例
        double changeRate = 0.0;
        if (oldLength > 0 || newLength > 0) {
            int maxLength = Math.max(oldLength, newLength);
            int diff = Math.abs(newLength - oldLength);
            changeRate = maxLength > 0 ? (double) diff / maxLength : 0.0;
        }

        // 判断是否需要自动保存
        boolean needAutoSave = false;
        if (minutesSinceLastSave >= 10) {
            // 距离上次自动保存超过10分钟
            needAutoSave = true;
        } else if (changeRate > 0.05) {
            // 内容变化超过5%
            needAutoSave = true;
        }

        if (needAutoSave) {
            try {
                noteVersionService.autoSaveVersion(noteId);
                lastAutoSaveTimeMap.put(noteId, now);
                log.info("自动保存笔记版本，笔记ID：{}，时间间隔：{}分钟，内容变化率：{}%",
                        noteId, minutesSinceLastSave, changeRate * 100);
            } catch (Exception e) {
                log.error("自动保存笔记版本失败，笔记ID：{}", noteId, e);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNote(Long userId, Long noteId) {
        // 查询笔记
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }

        // 检查权限
        checkNotePermission(userId, note);

        // 软删除
        note.setStatus(0);
        noteMapper.updateById(note);
        log.info("删除笔记成功，笔记ID：{}", noteId);
    }

    @Override
    public Note getNoteDetail(Long userId, Long noteId) {
        // 查询笔记
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }

        // 检查权限
        checkNotePermission(userId, note);

        // 校验状态
        if (note.getStatus() == 0) {
            throw new BusinessException("笔记已被删除");
        }

        // 增加查看次数
        note.setViewCount(note.getViewCount() + 1);
        noteMapper.updateById(note);

        return note;
    }

    @Override
    public IPage<Note> listNotes(Long userId, NoteQueryRequest query) {
        // 构建查询条件
        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        // 注意：不手动添加 eq(Note::getStatus, 1)，@TableLogic 已自动处理逻辑删除

        // 如果指定了空间ID，查询该空间的笔记
        if (query.getSpaceId() != null && query.getSpaceId() > 0) {
            // 检查用户是否有该空间的查看权限
            checkSpacePermission(userId, query.getSpaceId(), "viewer");
            queryWrapper.eq(Note::getSpaceId, query.getSpaceId());
        } else {
            // 查询个人笔记（spaceId为0或null表示个人笔记）
            queryWrapper.eq(Note::getUserId, userId);
            queryWrapper.and(w -> w.isNull(Note::getSpaceId).or().eq(Note::getSpaceId, 0));
        }

        // 关键词搜索（标题、内容、标签）
        if (StringUtils.hasText(query.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Note::getTitle, query.getKeyword())
                    .or()
                    .like(Note::getContent, query.getKeyword())
                    .or()
                    .like(Note::getTags, query.getKeyword())
            );
        }

        // 文件夹筛选
        if (query.getFolderId() != null) {
            queryWrapper.eq(Note::getFolderId, query.getFolderId());
        }

        // 标签筛选
        if (StringUtils.hasText(query.getTag())) {
            queryWrapper.like(Note::getTags, query.getTag());
        }

        // 收藏筛选
        if (query.getIsFavorite() != null) {
            queryWrapper.eq(Note::getIsFavorite, query.getIsFavorite());
        }

        // 置顶筛选
        if (query.getIsTop() != null) {
            queryWrapper.eq(Note::getIsTop, query.getIsTop());
        }

        // 时间范围筛选
        if (query.getStartTime() != null) {
            queryWrapper.ge(Note::getCreatedAt, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            queryWrapper.le(Note::getCreatedAt, query.getEndTime());
        }

        // 排序
        applySorting(queryWrapper, query);

        // 分页查询
        Page<Note> page = new Page<>(query.getPage(), query.getSize());
        return noteMapper.selectPage(page, queryWrapper);
    }

    /**
     * 应用排序
     *
     * @param queryWrapper 查询条件
     * @param query        查询请求
     */
    private void applySorting(LambdaQueryWrapper<Note> queryWrapper, NoteQueryRequest query) {
        String sortBy = query.getSortBy();
        String sortOrder = query.getSortOrder();

        // 置顶优先
        queryWrapper.orderByDesc(Note::getIsTop);

        // 根据排序字段和方向排序
        boolean isAsc = "asc".equalsIgnoreCase(sortOrder);

        switch (sortBy) {
            case "createdAt":
                if (isAsc) {
                    queryWrapper.orderByAsc(Note::getCreatedAt);
                } else {
                    queryWrapper.orderByDesc(Note::getCreatedAt);
                }
                break;
            case "viewCount":
                if (isAsc) {
                    queryWrapper.orderByAsc(Note::getViewCount);
                } else {
                    queryWrapper.orderByDesc(Note::getViewCount);
                }
                break;
            case "updatedAt":
            default:
                if (isAsc) {
                    queryWrapper.orderByAsc(Note::getUpdatedAt);
                } else {
                    queryWrapper.orderByDesc(Note::getUpdatedAt);
                }
                break;
        }
    }

    @Override
    public IPage<Note> searchNotes(Long userId, String keyword) {
        // 构建查询条件
        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getUserId, userId);
        // @TableLogic 已自动处理逻辑删除，不需要手动加 status 条件

        // 关键词搜索（标题、内容、标签）
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Note::getTitle, keyword)
                    .or()
                    .like(Note::getContent, keyword)
                    .or()
                    .like(Note::getTags, keyword)
            );
        }

        // 排序：按更新时间倒序，置顶优先
        queryWrapper.orderByDesc(Note::getIsTop);
        queryWrapper.orderByDesc(Note::getUpdatedAt);

        // 分页查询（默认每页20条）
        Page<Note> page = new Page<>(1, 20);
        return noteMapper.selectPage(page, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleFavorite(Long userId, Long noteId) {
        // 查询笔记
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }

        // 检查权限
        checkNotePermission(userId, note);

        // 校验状态
        if (note.getStatus() == 0) {
            throw new BusinessException("笔记已被删除");
        }

        // 切换收藏状态
        note.setIsFavorite(note.getIsFavorite() == 1 ? 0 : 1);
        noteMapper.updateById(note);
        log.info("切换笔记收藏状态，笔记ID：{}，收藏状态：{}", noteId, note.getIsFavorite());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleTop(Long userId, Long noteId) {
        // 查询笔记
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }

        // 检查权限
        checkNotePermission(userId, note);

        // 校验状态
        if (note.getStatus() == 0) {
            throw new BusinessException("笔记已被删除");
        }

        // 切换置顶状态
        note.setIsTop(note.getIsTop() == 1 ? 0 : 1);
        noteMapper.updateById(note);
        log.info("切换笔记置顶状态，笔记ID：{}，置顶状态：{}", noteId, note.getIsTop());
    }

    /**
     * 检查笔记权限
     *
     * @param userId 用户ID
     * @param note   笔记
     */
    private void checkNotePermission(Long userId, Note note) {
        // 个人笔记，只有作者可以操作
        if (note.getSpaceId() == null || note.getSpaceId() == 0) {
            if (!note.getUserId().equals(userId)) {
                throw new BusinessException("无权限操作该笔记");
            }
        } else {
            // 空间笔记，检查用户是否有该空间的编辑权限
            checkSpacePermission(userId, note.getSpaceId(), "editor");
        }
    }

    /**
     * 检查空间权限
     *
     * @param userId        用户ID
     * @param spaceId       空间ID
     * @param requiredRole  要求的最低角色
     */
    private void checkSpacePermission(Long userId, Long spaceId, String requiredRole) {
        LambdaQueryWrapper<SpaceMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(SpaceMember::getSpaceId, spaceId);
        memberWrapper.eq(SpaceMember::getUserId, userId);
        SpaceMember member = spaceMemberMapper.selectOne(memberWrapper);

        if (member == null) {
            throw new BusinessException("不是该空间的成员");
        }

        if (member.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        int userLevel = getRoleLevel(member.getRole());
        int requiredLevel = getRoleLevel(requiredRole);

        if (userLevel < requiredLevel) {
            throw new BusinessException("权限不足");
        }
    }

    /**
     * 获取角色等级
     *
     * @param role 角色
     * @return 角色等级
     */
    private int getRoleLevel(String role) {
        switch (role) {
            case "owner":
                return ROLE_LEVEL_OWNER;
            case "admin":
                return ROLE_LEVEL_ADMIN;
            case "editor":
                return ROLE_LEVEL_EDITOR;
            case "viewer":
                return ROLE_LEVEL_VIEWER;
            default:
                return 0;
        }
    }

}
