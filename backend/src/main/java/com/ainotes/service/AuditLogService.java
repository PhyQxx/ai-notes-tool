package com.ainotes.service;

import com.ainotes.entity.AuditLog;
import com.baomidou.mybatisplus.core.metadata.IPage;

import org.springframework.scheduling.annotation.Async;

public interface AuditLogService {
    @Async
    void log(Long userId, String username, String action, String targetType, Long targetId, String detail, String ip);
    IPage<AuditLog> queryLogs(Integer page, Integer size, Long userId, String action, String startTime, String endTime);
}
