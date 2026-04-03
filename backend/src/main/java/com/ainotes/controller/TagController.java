package com.ainotes.controller;

import com.ainotes.common.result.Result;
import com.ainotes.entity.Note;
import com.ainotes.entity.TagColor;
import com.ainotes.mapper.NoteMapper;
import com.ainotes.mapper.TagColorMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final NoteMapper noteMapper;
    private final TagColorMapper tagColorMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TAG_CLOUD_CACHE_PREFIX = "tags:cloud:";
    private static final long TAG_CACHE_TTL_MINUTES = 5;

    @GetMapping("/cloud")
    @SuppressWarnings("unchecked")
    public Result<List<Map<String, Object>>> getTagCloud(
            @RequestParam(required = false) Long spaceId) {
        // 尝试从 Redis 缓存获取
        String cacheKey = TAG_CLOUD_CACHE_PREFIX + (spaceId != null ? spaceId : "global");
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof List) {
                return Result.success((List<Map<String, Object>>) cached);
            }
        } catch (Exception e) {
            log.warn("标签云缓存读取失败", e);
        }

        // 查询数据库
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(Note::getTags).ne(Note::getTags, "");
        if (spaceId != null) {
            wrapper.eq(Note::getSpaceId, spaceId);
        }
        List<Note> notes = noteMapper.selectList(wrapper);

        Map<String, Long> tagCount = new LinkedHashMap<>();
        for (Note note : notes) {
            if (note.getTags() != null && !note.getTags().isEmpty()) {
                for (String tag : note.getTags().split(",")) {
                    String t = tag.trim();
                    if (!t.isEmpty()) {
                        tagCount.merge(t, 1L, Long::sum);
                    }
                }
            }
        }

        List<Map<String, Object>> result = tagCount.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .sorted((a, b) -> (int) ((long) b.get("count") - (long) a.get("count")))
                .collect(Collectors.toList());

        // 写入缓存
        try {
            redisTemplate.opsForValue().set(cacheKey, result, TAG_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("标签云缓存写入失败", e);
        }

        return Result.success(result);
    }

    @GetMapping("/colors")
    public Result<List<TagColor>> getTagColors() {
        return Result.success(tagColorMapper.selectList(null));
    }

    @PutMapping("/colors")
    public Result<Void> setTagColors(@RequestBody List<TagColor> colors) {
        for (TagColor tc : colors) {
            if (tc.getName() == null || tc.getColor() == null) continue;
            TagColor existing = tagColorMapper.selectOne(
                    new LambdaQueryWrapper<TagColor>().eq(TagColor::getName, tc.getName()));
            if (existing != null) {
                existing.setColor(tc.getColor());
                tagColorMapper.updateById(existing);
            } else {
                TagColor nc = new TagColor();
                nc.setName(tc.getName());
                nc.setColor(tc.getColor());
                tagColorMapper.insert(nc);
            }
        }
        return Result.success("更新成功", null);
    }

    @PostMapping("/batch")
    public Result<Void> batchAddTags(@RequestBody BatchTagRequest req) {
        for (Long noteId : req.getNoteIds()) {
            Note note = noteMapper.selectById(noteId);
            if (note == null) continue;
            Set<String> tagSet = new LinkedHashSet<>();
            if (note.getTags() != null && !note.getTags().isEmpty()) {
                Collections.addAll(tagSet, note.getTags().split(","));
            }
            for (String t : req.getTags()) {
                String trimmed = t.trim();
                if (!trimmed.isEmpty()) tagSet.add(trimmed);
            }
            note.setTags(String.join(",", tagSet));
            noteMapper.updateById(note);
        }
        return Result.success("标签添加成功", null);
    }

    @Data
    public static class BatchTagRequest {
        private List<Long> noteIds;
        private List<String> tags;
    }
}
