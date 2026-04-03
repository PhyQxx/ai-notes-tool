package com.ainotes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ainotes.common.exception.BusinessException;
import com.ainotes.entity.NoteTemplate;
import com.ainotes.mapper.NoteTemplateMapper;
import com.ainotes.service.NoteTemplateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteTemplateServiceImpl implements NoteTemplateService {

    private final NoteTemplateMapper templateMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<NoteTemplate> listTemplates(String category, Long spaceId) {
        LambdaQueryWrapper<NoteTemplate> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) {
            wrapper.eq(NoteTemplate::getCategory, category);
        }
        if (spaceId != null) {
            wrapper.and(w -> w.eq(NoteTemplate::getSpaceId, spaceId).or().isNull(NoteTemplate::getSpaceId));
        }
        wrapper.orderByAsc(NoteTemplate::getSortOrder);
        return templateMapper.selectList(wrapper);
    }

    @Override
    public NoteTemplate getTemplate(Long id) {
        NoteTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }
        return template;
    }

    @Override
    public Long createTemplate(Long userId, NoteTemplate template) {
        template.setCategory("custom");
        template.setCreatedBy(userId);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        templateMapper.insert(template);
        return template.getId();
    }

    @Override
    public void updateTemplate(Long userId, Long id, NoteTemplate template) {
        NoteTemplate existing = getTemplate(id);
        if ("system".equals(existing.getCategory())) {
            throw new BusinessException("系统模板不可修改");
        }
        template.setId(id);
        template.setUpdatedAt(LocalDateTime.now());
        templateMapper.updateById(template);
    }

    @Override
    public void deleteTemplate(Long userId, Long id) {
        NoteTemplate existing = getTemplate(id);
        if ("system".equals(existing.getCategory())) {
            throw new BusinessException("系统模板不可删除");
        }
        templateMapper.deleteById(id);
    }

    @Override
    public String applyTemplate(Long id, Map<String, String> variables) {
        NoteTemplate template = getTemplate(id);
        String content = template.getContent();

        // Built-in variables
        Map<String, String> allVars = new HashMap<>();
        if (variables != null) {
            allVars.putAll(variables);
        }
        allVars.putIfAbsent("日期", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        allVars.putIfAbsent("时间", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        allVars.putIfAbsent("星期", LocalDate.now().getDayOfWeek().toString());

        for (Map.Entry<String, String> entry : allVars.entrySet()) {
            content = content.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return content;
    }
}
