package com.ainotes.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_note_attachment")
public class NoteAttachment implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long noteId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private Long uploadedBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
