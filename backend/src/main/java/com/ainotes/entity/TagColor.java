package com.ainotes.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_tag_color")
public class TagColor {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String color;
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
