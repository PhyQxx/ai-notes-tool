package com.ainotes.controller;

import com.ainotes.common.result.Result;
import com.ainotes.entity.NoteTemplate;
import com.ainotes.service.NoteTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
@Tag(name = "笔记模板", description = "笔记模板管理")
public class NoteTemplateController {

    private final NoteTemplateService templateService;

    @GetMapping
    @Operation(summary = "获取模板列表")
    public Result<List<NoteTemplate>> listTemplates(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long spaceId) {
        return Result.success(templateService.listTemplates(category, spaceId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取模板详情")
    public Result<NoteTemplate> getTemplate(@PathVariable Long id) {
        return Result.success(templateService.getTemplate(id));
    }

    @PostMapping
    @Operation(summary = "创建自定义模板")
    public Result<Long> createTemplate(Authentication auth, @RequestBody NoteTemplate template) {
        Long userId = Long.parseLong(auth.getName());
        return Result.success(templateService.createTemplate(userId, template));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新模板")
    public Result<Void> updateTemplate(Authentication auth, @PathVariable Long id, @RequestBody NoteTemplate template) {
        Long userId = Long.parseLong(auth.getName());
        templateService.updateTemplate(userId, id, template);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除模板")
    public Result<Void> deleteTemplate(Authentication auth, @PathVariable Long id) {
        Long userId = Long.parseLong(auth.getName());
        templateService.deleteTemplate(userId, id);
        return Result.success();
    }

    @PostMapping("/{id}/apply")
    @Operation(summary = "应用模板")
    public Result<Map<String, String>> applyTemplate(@PathVariable Long id, @RequestBody(required = false) Map<String, String> variables) {
        String content = templateService.applyTemplate(id, variables);
        return Result.success(Map.of("content", content));
    }
}
