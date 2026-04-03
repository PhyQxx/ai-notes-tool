package com.ainotes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ainotes.common.exception.BusinessException;
import com.ainotes.dto.query.NoteQueryRequest;
import com.ainotes.dto.request.CreateNoteRequest;
import com.ainotes.dto.request.UpdateNoteRequest;
import com.ainotes.dto.response.SearchResultDTO;
import com.ainotes.entity.Note;
import com.ainotes.entity.NoteLink;
import com.ainotes.entity.SpaceMember;
import com.ainotes.mapper.NoteMapper;
import com.ainotes.mapper.NoteLinkMapper;
import com.ainotes.mapper.SpaceMemberMapper;
import com.ainotes.service.NoteLinkService;
import com.ainotes.service.NoteService;
import com.ainotes.service.NoteVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
    private final NoteLinkMapper noteLinkMapper;
    private final NoteVersionService noteVersionService;
    private final SpaceMemberMapper spaceMemberMapper;
    private final NoteLinkService noteLinkService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final com.ainotes.service.AuditLogService auditLogService;

    private static final String NOTE_CACHE_KEY = "note:detail:";
    private static final long NOTE_CACHE_TTL_MINUTES = 10;
    private static final String TAG_CLOUD_CACHE_KEY = "tags:cloud:";
    private static final long TAG_CACHE_TTL_MINUTES = 5;

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
        note.setTags(convertTagsToString(request.getTags()));
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

        // 同步双向链接
        try { noteLinkService.syncNoteLinks(note.getId(), note.getContent(), note.getTitle()); } catch (Exception e) { log.warn("同步笔记链接失败", e); }

        auditLogService.log(userId, null, "CREATE_NOTE", "note", note.getId(), "创建笔记: " + note.getTitle(), null);
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
            note.setTags(convertTagsToString(request.getTags()));
        }

        // 更新笔记
        noteMapper.updateById(note);

        // 同步双向链接
        try { noteLinkService.syncNoteLinks(noteId, note.getContent(), note.getTitle()); } catch (Exception e) { log.warn("同步笔记链接失败", e); }

        // 检查是否需要自动保存版本
        checkAndAutoSaveVersion(noteId, oldContent);

        log.info("更新笔记成功，笔记ID：{}", noteId);

        // 清除笔记详情缓存
        evictNoteCache(noteId);
        // 清除标签云缓存
        evictTagCloudCache();
        auditLogService.log(userId, null, "UPDATE_NOTE", "note", noteId, "更新笔记: " + note.getTitle(), null);
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

        // 移入回收站
        note.setDeleted(1);
        note.setDeletedAt(LocalDateTime.now());
        noteMapper.updateById(note);
        log.info("笔记移入回收站，笔记ID：{}", noteId);

        // 清除笔记详情缓存
        evictNoteCache(noteId);
        // 清除标签云缓存
        evictTagCloudCache();
        auditLogService.log(userId, null, "DELETE_NOTE", "note", noteId, "删除笔记(软删除): " + note.getTitle(), null);
    }

    @Override
    public Note getNoteDetail(Long userId, Long noteId) {
        // 尝试从 Redis 缓存获取
        String cacheKey = NOTE_CACHE_KEY + noteId;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof Note) {
                return (Note) cached;
            }
        } catch (Exception e) {
            log.warn("Redis缓存读取失败，回退到数据库查询", e);
        }

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

        // 写入 Redis 缓存
        try {
            redisTemplate.opsForValue().set(cacheKey, note, NOTE_CACHE_TTL_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis缓存写入失败", e);
        }

        return note;
    }

    @Override
    public IPage<Note> listNotes(Long userId, NoteQueryRequest query) {
        // 构建查询条件
        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        // 注意：不手动添加 eq(Note::getStatus, 1)，@TableLogic 已自动处理逻辑删除
        // 排除回收站中的笔记
        queryWrapper.eq(Note::getDeleted, 0);

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
    public IPage<SearchResultDTO> fullTextSearch(Long userId, String keyword, String scope) {
        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getUserId, userId);

        if (StringUtils.hasText(keyword)) {
            if ("title".equals(scope)) {
                queryWrapper.like(Note::getTitle, keyword);
            } else if ("content".equals(scope)) {
                queryWrapper.like(Note::getContent, keyword);
            } else {
                // all: search title, content, tags
                queryWrapper.and(wrapper -> wrapper
                        .like(Note::getTitle, keyword)
                        .or()
                        .like(Note::getContent, keyword)
                        .or()
                        .like(Note::getTags, keyword)
                );
            }
        }

        queryWrapper.orderByDesc(Note::getIsTop);
        queryWrapper.orderByDesc(Note::getUpdatedAt);

        Page<Note> page = new Page<>(1, 50);
        IPage<Note> notePage = noteMapper.selectPage(page, queryWrapper);

        // Convert to SearchResultDTO with highlighting
        List<SearchResultDTO> records = notePage.getRecords().stream().map(note -> {
            SearchResultDTO dto = new SearchResultDTO();
            dto.setId(note.getId());
            dto.setTitle(note.getTitle());
            dto.setContentType(note.getContentType());
            dto.setIsFavorite(note.getIsFavorite());
            dto.setIsTop(note.getIsTop());
            dto.setTags(note.getTags());
            dto.setUpdatedAt(note.getUpdatedAt());

            String keywordLower = keyword.toLowerCase();
            int matchCount = 0;

            // Title highlight
            String titleText = note.getTitle() != null ? note.getTitle() : "";
            String titleLower = titleText.toLowerCase();
            int titleMatches = countOccurrences(titleLower, keywordLower);
            matchCount += titleMatches;
            dto.setTitleHighlight(highlightKeyword(titleText, keyword));

            // Content preview
            String content = note.getContent() != null ? note.getContent() : "";
            // Strip HTML tags for preview
            String plainContent = content.replaceAll("<[^>]*>", "");
            int contentMatches = countOccurrences(plainContent.toLowerCase(), keywordLower);
            matchCount += contentMatches;
            dto.setContentPreview(generatePreview(plainContent, keyword, 120));
            dto.setContentPreview(highlightKeyword(dto.getContentPreview(), keyword));

            // Tag matches
            String tags = note.getTags() != null ? note.getTags() : "";
            matchCount += countOccurrences(tags.toLowerCase(), keywordLower);

            dto.setMatchCount(matchCount);
            return dto;
        }).sorted((a, b) -> {
            // Sort by match count desc, then by updatedAt desc
            if (b.getMatchCount() != a.getMatchCount()) {
                return b.getMatchCount() - a.getMatchCount();
            }
            return b.getUpdatedAt().compareTo(a.getUpdatedAt());
        }).collect(java.util.stream.Collectors.toList());

        // Build result page
        Page<SearchResultDTO> resultPage = new Page<>(notePage.getCurrent(), notePage.getSize(), notePage.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    private String highlightKeyword(String text, String keyword) {
        if (text == null || keyword == null || keyword.isEmpty()) return text;
        String escaped = keyword.replaceAll("([\\\\\\[\\](){}*+?.,^$|])", "\\\\$1");
        return text.replaceAll("(?i)(" + escaped + ")", "<em>$1</em>");
    }

    private String generatePreview(String content, String keyword, int maxLen) {
        if (content == null || content.isEmpty()) return "";
        if (keyword == null || keyword.isEmpty()) {
            return content.length() > maxLen ? content.substring(0, maxLen) + "..." : content;
        }
        String keywordLower = keyword.toLowerCase();
        String contentLower = content.toLowerCase();
        int idx = contentLower.indexOf(keywordLower);
        if (idx < 0) {
            return content.length() > maxLen ? content.substring(0, maxLen) + "..." : content;
        }
        int contextRadius = Math.max(20, (maxLen - keyword.length()) / 2);
        int start = Math.max(0, idx - contextRadius);
        int end = Math.min(content.length(), idx + keyword.length() + contextRadius);
        String preview = content.substring(start, end);
        if (start > 0) preview = "..." + preview;
        if (end < content.length()) preview = preview + "...";
        return preview;
    }

    private int countOccurrences(String text, String keyword) {
        if (text == null || keyword == null || keyword.isEmpty()) return 0;
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(keyword, idx)) != -1) {
            count++;
            idx += keyword.length();
        }
        return count;
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

    /**
     * 将 tags 转换为逗号分隔字符串（兼容前端数组和字符串）
     */
    @SuppressWarnings("unchecked")
    private String convertTagsToString(Object tags) {
        if (tags == null) return null;
        if (tags instanceof String) return (String) tags;
        if (tags instanceof java.util.List) {
            return String.join(",", (java.util.List<String>) tags);
        }
        return tags.toString();
    }

    @Override
    public IPage<Note> listTrash(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getDeleted, 1);
        queryWrapper.eq(Note::getUserId, userId);
        queryWrapper.orderByDesc(Note::getDeletedAt);
        Page<Note> pageObj = new Page<>(page, size);
        return noteMapper.selectPage(pageObj, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreNote(Long userId, Long noteId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作该笔记");
        }
        if (note.getDeleted() == null || note.getDeleted() == 0) {
            throw new BusinessException("该笔记不在回收站中");
        }
        note.setDeleted(0);
        note.setDeletedAt(null);
        noteMapper.updateById(note);
        log.info("从回收站恢复笔记，笔记ID：{}", noteId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void permanentDeleteNote(Long userId, Long noteId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作该笔记");
        }
        if (note.getDeleted() == null || note.getDeleted() == 0) {
            throw new BusinessException("该笔记不在回收站中");
        }
        // 物理删除
        noteMapper.deleteById(noteId);
        log.info("彻底删除笔记，笔记ID：{}", noteId);

        // 清除缓存
        evictNoteCache(noteId);
        evictTagCloudCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void emptyTrash(Long userId) {
        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getDeleted, 1);
        queryWrapper.eq(Note::getUserId, userId);
        List<Note> trashNotes = noteMapper.selectList(queryWrapper);
        for (Note note : trashNotes) {
            noteMapper.deleteById(note.getId());
        }
        log.info("清空回收站，用户ID：{}，删除数量：{}", userId, trashNotes.size());

        // 清除标签云缓存
        evictTagCloudCache();
    }

    private void evictNoteCache(Long noteId) {
        try {
            redisTemplate.delete(NOTE_CACHE_KEY + noteId);
        } catch (Exception e) {
            log.warn("清除笔记缓存失败", e);
        }
    }

    private void evictTagCloudCache() {
        try {
            var keys = redisTemplate.keys(TAG_CLOUD_CACHE_KEY + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("清除标签云缓存失败", e);
        }
    }

    @Override
    public List<Note> recommendNotes(Long userId, Long noteId, Integer limit) {
        if (limit == null || limit <= 0) limit = 5;
        if (limit > 20) limit = 20;

        // 获取用户所有未删除笔记
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId)
               .eq(Note::getDeleted, 0)
               .isNotNull(Note::getTags)
               .ne(Note::getTags, "");
        List<Note> allNotes = noteMapper.selectList(wrapper);

        if (allNotes.isEmpty()) return Collections.emptyList();

        Set<String> targetTags = new HashSet<>();
        Note sourceNote = null;

        if (noteId != null) {
            sourceNote = allNotes.stream().filter(n -> n.getId().equals(noteId)).findFirst().orElse(null);
            if (sourceNote != null) {
                targetTags = parseTags(sourceNote.getTags());
            }
        } else {
            // 基于最近编辑的笔记的标签
            LambdaQueryWrapper<Note> recentWrapper = new LambdaQueryWrapper<>();
            recentWrapper.eq(Note::getUserId, userId)
                        .eq(Note::getDeleted, 0)
                        .isNotNull(Note::getTags)
                        .ne(Note::getTags, "")
                        .orderByDesc(Note::getUpdatedAt)
                        .last("LIMIT 5");
            List<Note> recentNotes = noteMapper.selectList(recentWrapper);
            for (Note n : recentNotes) {
                targetTags.addAll(parseTags(n.getTags()));
            }
        }

        if (targetTags.isEmpty()) {
            // 无标签，返回最近更新的笔记
            LambdaQueryWrapper<Note> fallbackWrapper = new LambdaQueryWrapper<>();
            fallbackWrapper.eq(Note::getUserId, userId)
                          .eq(Note::getDeleted, 0)
                          .orderByDesc(Note::getUpdatedAt)
                          .last("LIMIT " + limit);
            if (noteId != null) {
                fallbackWrapper.ne(Note::getId, noteId);
            }
            return noteMapper.selectList(fallbackWrapper);
        }

        // 计算每篇笔记的相似度得分
        LocalDateTime now = LocalDateTime.now();
        List<NoteScore> scored = new ArrayList<>();
        for (Note note : allNotes) {
            if (noteId != null && note.getId().equals(noteId)) continue;
            Set<String> noteTags = parseTags(note.getTags());
            Set<String> overlap = new HashSet<>(noteTags);
            overlap.retainAll(targetTags);

            if (overlap.isEmpty()) continue;

            // 标签重叠度得分
            double tagScore = (double) overlap.size() / (double) targetTags.size();
            // 时间权重：最近30天的笔记权重更高
            double timeWeight = 1.0;
            if (note.getUpdatedAt() != null) {
                long daysDiff = java.time.Duration.between(note.getUpdatedAt(), now).toDays();
                if (daysDiff < 7) timeWeight = 2.0;
                else if (daysDiff < 30) timeWeight = 1.5;
                else if (daysDiff < 90) timeWeight = 1.2;
            }
            scored.add(new NoteScore(note, tagScore * timeWeight));
        }

        scored.sort((a, b) -> Double.compare(b.score, a.score));
        return scored.stream().limit(limit).map(ns -> ns.note).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getGraphData(Long userId) {
        // 获取用户所有未删除笔记
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId).eq(Note::getDeleted, 0);
        List<Note> notes = noteMapper.selectList(wrapper);

        Set<Long> noteIds = notes.stream().map(Note::getId).collect(Collectors.toSet());

        // 节点
        List<Map<String, Object>> nodes = notes.stream().map(n -> {
            Map<String, Object> node = new HashMap<>();
            node.put("id", n.getId());
            node.put("title", n.getTitle() != null ? n.getTitle() : "无标题");
            node.put("tags", parseTags(n.getTags()));
            return node;
        }).collect(Collectors.toList());

        // 边 - 双向链接
        List<Map<String, Object>> edges = new ArrayList<>();
        LambdaQueryWrapper<NoteLink> linkWrapper = new LambdaQueryWrapper<>();
        linkWrapper.in(NoteLink::getSourceNoteId, noteIds)
                   .or(w -> w.in(NoteLink::getTargetNoteId, noteIds));
        List<NoteLink> links = noteLinkMapper.selectList(linkWrapper);
        for (NoteLink link : links) {
            if (!noteIds.contains(link.getSourceNoteId()) || !noteIds.contains(link.getTargetNoteId())) continue;
            if (link.getSourceNoteId().equals(link.getTargetNoteId())) continue;
            Map<String, Object> edge = new HashMap<>();
            edge.put("source", link.getSourceNoteId());
            edge.put("target", link.getTargetNoteId());
            edge.put("type", "link");
            edges.add(edge);
        }

        // 边 - 标签关系（共享>=2个标签）
        Map<Long, Set<String>> noteTagMap = new HashMap<>();
        for (Note n : notes) {
            noteTagMap.put(n.getId(), parseTags(n.getTags()));
        }
        List<Long> idList = new ArrayList<>(noteIds);
        Set<String> seenEdges = new HashSet<>();
        for (int i = 0; i < idList.size(); i++) {
            for (int j = i + 1; j < idList.size(); j++) {
                Long a = idList.get(i), b = idList.get(j);
                Set<String> tagsA = noteTagMap.getOrDefault(a, Collections.emptySet());
                Set<String> tagsB = noteTagMap.getOrDefault(b, Collections.emptySet());
                Set<String> overlap = new HashSet<>(tagsA);
                overlap.retainAll(tagsB);
                if (overlap.size() >= 2) {
                    String key = Math.min(a, b) + "-" + Math.max(a, b) + "-tag";
                    if (!seenEdges.contains(key)) {
                        seenEdges.add(key);
                        Map<String, Object> edge = new HashMap<>();
                        edge.put("source", a);
                        edge.put("target", b);
                        edge.put("type", "tag");
                        edges.add(edge);
                    }
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("nodes", nodes);
        result.put("edges", edges);
        return result;
    }

    private Set<String> parseTags(String tags) {
        if (tags == null || tags.isBlank()) return Collections.emptySet();
        return Arrays.stream(tags.split("[,，]"))
                     .map(String::trim)
                     .filter(s -> !s.isEmpty())
                     .collect(Collectors.toSet());
    }

    private static class NoteScore {
        Note note;
        double score;
        NoteScore(Note note, double score) {
            this.note = note;
            this.score = score;
        }
    }

}
