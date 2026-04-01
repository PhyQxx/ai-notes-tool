package com.ainotes.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 笔记实体类
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Data
@TableName("t_note")
public class Note implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 笔记ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 创建用户ID
     */
    private Long userId;

    /**
     * 所属空间ID
     */
    private Long spaceId;

    /**
     * 笔记标题
     */
    private String title;

    /**
     * 笔记内容
     */
    private String content;

    /**
     * 内容类型：markdown-Markdown编辑器，richtext-富文本编辑器
     */
    private String contentType;

    /**
     * 所属文件夹ID
     */
    private Long folderId;

    /**
     * 标签(逗号分隔)
     */
    private String tags;

    /**
     * 是否收藏：1-是，0-否
     */
    private Integer isFavorite;

    /**
     * 是否置顶：1-是，0-否
     */
    private Integer isTop;

    /**
     * 查看次数
     */
    private Integer viewCount;

    /**
     * 状态：1-正常，0-删除(软删除)
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
