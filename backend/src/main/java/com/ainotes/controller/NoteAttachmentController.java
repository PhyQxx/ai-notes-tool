package com.ainotes.controller;

import com.ainotes.common.result.Result;
import com.ainotes.entity.NoteAttachment;
import com.ainotes.service.NoteAttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/notes/{noteId}/attachments")
@RequiredArgsConstructor
@Tag(name = "笔记附件", description = "笔记附件管理")
public class NoteAttachmentController {

    private final NoteAttachmentService noteAttachmentService;

    @PostMapping
    @Operation(summary = "上传附件")
    public Result<NoteAttachment> upload(@PathVariable Long noteId,
                                         @RequestParam("file") MultipartFile file,
                                         Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success("上传成功", noteAttachmentService.uploadAttachment(userId, noteId, file));
    }

    @GetMapping
    @Operation(summary = "获取附件列表")
    public Result<List<NoteAttachment>> list(@PathVariable Long noteId) {
        return Result.success(noteAttachmentService.listByNoteId(noteId));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "下载附件")
    public ResponseEntity<byte[]> download(@PathVariable Long noteId, @PathVariable Long id) {
        NoteAttachment att = noteAttachmentService.listByNoteId(noteId).stream()
                .filter(a -> a.getId().equals(id)).findFirst().orElse(null);
        if (att == null) return ResponseEntity.notFound().build();
        byte[] data = noteAttachmentService.downloadAttachment(id);
        String encoded = URLEncoder.encode(att.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(data.length)
                .body(data);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除附件")
    public Result<Void> delete(@PathVariable Long noteId, @PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        noteAttachmentService.deleteAttachment(userId, id);
        return Result.success("删除成功", null);
    }
}
