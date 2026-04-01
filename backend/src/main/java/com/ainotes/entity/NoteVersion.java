package com.ainotes.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 笔记版本实体类
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Data
@TableName("t_note_version")
public class NoteVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 版本ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 笔记ID
     */
    private Long noteId;

    /**
     * 版本号
     */
    private Integer versionNo;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 版本备注
     */
    private String remark;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

}
