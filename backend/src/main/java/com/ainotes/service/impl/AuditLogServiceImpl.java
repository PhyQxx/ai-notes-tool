package com.ainotes.service.impl;

import com.ainotes.entity.AuditLog;
import com.ainotes.mapper.AuditLogMapper;
import com.ainotes.service.AuditLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogMapper auditLogMapper;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void log(Long userId, String username, String action, String targetType, Long targetId, String detail, String ip) {
        AuditLog al = new AuditLog();
        al.setUserId(userId);
        al.setUsername(username);
        al.setAction(action);
        al.setTargetType(targetType);
        al.setTargetId(targetId);
        al.setDetail(detail);
        al.setIp(ip);
        al.setCreatedAt(LocalDateTime.now());
        auditLogMapper.insert(al);
    }

    @Override
    public IPage<AuditLog> queryLogs(Integer page, Integer size, Long userId, String action, String startTime, String endTime) {
        LambdaQueryWrapper<AuditLog> qw = new LambdaQueryWrapper<>();
        if (userId != null) qw.eq(AuditLog::getUserId, userId);
        if (StringUtils.hasText(action)) qw.eq(AuditLog::getAction, action);
        if (StringUtils.hasText(startTime)) qw.ge(AuditLog::getCreatedAt, LocalDateTime.parse(startTime, FMT));
        if (StringUtils.hasText(endTime)) qw.le(AuditLog::getCreatedAt, LocalDateTime.parse(endTime, FMT));
        qw.orderByDesc(AuditLog::getCreatedAt);
        return auditLogMapper.selectPage(new Page<>(page, size), qw);
    }
}
