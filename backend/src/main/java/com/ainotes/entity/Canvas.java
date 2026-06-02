package com.ainotes.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_canvas")
public class Canvas {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String data;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
