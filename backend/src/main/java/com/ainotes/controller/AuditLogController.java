package com.ainotes.controller;

import com.ainotes.common.result.Result;
import com.ainotes.entity.AuditLog;
import com.ainotes.service.AuditLogService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
@Tag(name = "操作日志", description = "审计日志查询")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "查询操作日志")
    public Result<IPage<AuditLog>> query(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return Result.success(auditLogService.queryLogs(page, size, userId, action, startTime, endTime));
    }
}
