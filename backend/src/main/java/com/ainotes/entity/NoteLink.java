package com.ainotes.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 笔记双向链接实体
 */
@Data
@TableName("t_note_link")
public class NoteLink implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sourceNoteId;

    private Long targetNoteId;

    private String sourceTitle;

    private String targetTitle;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
