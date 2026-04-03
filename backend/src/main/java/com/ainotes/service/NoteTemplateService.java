package com.ainotes.service;

import com.ainotes.entity.NoteTemplate;

import java.util.List;
import java.util.Map;

public interface NoteTemplateService {

    List<NoteTemplate> listTemplates(String category, Long spaceId);

    NoteTemplate getTemplate(Long id);

    Long createTemplate(Long userId, NoteTemplate template);

    void updateTemplate(Long userId, Long id, NoteTemplate template);

    void deleteTemplate(Long userId, Long id);

    String applyTemplate(Long id, Map<String, String> variables);
}
