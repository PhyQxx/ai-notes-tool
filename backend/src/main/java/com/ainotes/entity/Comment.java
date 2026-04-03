package com.ainotes.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论实体类
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Data
@TableName("t_comment")
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 笔记ID
     */
    private Long noteId;

    /**
     * 评论用户ID
     */
    private Long userId;

    /**
     * 父评论ID(0为顶级评论)
     */
    private Long parentId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * @提及的用户ID列表
     */
    private String mentionUserIds;

    /**
     * 评论状态：open/resolved
     */
    private String resolveStatus;

    /**
     * 被评论的文字
     */
    private String positionText;

    /**
     * 状态：1-正常，0-删除
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

}
