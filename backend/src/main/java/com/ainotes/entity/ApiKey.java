package com.ainotes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_api_key")
public class ApiKey {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String apiKey;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Boolean active;
}
