package com.ainotes.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 团队空间实体类
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Data
@TableName("t_space")
public class Space implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 空间ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 空间名称
     */
    private String name;

    /**
     * 空间描述
     */
    private String description;

    /**
     * 所有者用户ID
     */
    private Long ownerId;

    /**
     * 成员数量
     */
    private Integer memberCount;

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
