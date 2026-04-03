package com.ainotes.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ainotes.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
