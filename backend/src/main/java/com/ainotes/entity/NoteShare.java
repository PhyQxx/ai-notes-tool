package com.ainotes.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_note_share")
public class NoteShare implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long noteId;
    private String slug;
    private String password;
    private Integer viewCount;
    private LocalDateTime expireAt;
    private LocalDateTime createdAt;
    private Long createdBy;
}
