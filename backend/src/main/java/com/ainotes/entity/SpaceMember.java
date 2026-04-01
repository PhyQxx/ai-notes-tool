package com.ainotes.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 空间成员实体类
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Data
@TableName("t_space_member")
public class SpaceMember implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成员ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 空间ID
     */
    private Long spaceId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色：owner-所有者，admin-管理员，editor-编辑者，viewer-查看者
     */
    private String role;

    /**
     * 状态：1-正常，0-禁用
     */
    private Integer status;

    /**
     * 加入时间
     */
    private LocalDateTime joinedAt;

}
