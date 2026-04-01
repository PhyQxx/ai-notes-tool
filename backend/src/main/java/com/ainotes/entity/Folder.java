package com.ainotes.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件夹实体类
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Data
@TableName(value = "t_folder", autoResultMap = true)
public class Folder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件夹ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 所属空间ID
     */
    private Long spaceId;

    /**
     * 文件夹名称
     */
    private String name;

    /**
     * 父文件夹ID，0表示根目录
     */
    private Long parentId;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 子文件夹列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<Folder> children;

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
