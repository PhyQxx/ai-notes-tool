package com.ainotes.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 空间响应DTO
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Data
public class SpaceResponse {

    /**
     * 空间ID
     */
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
     * 所有者名称
     */
    private String ownerName;

    /**
     * 所有者昵称
     */
    private String ownerNickname;

    /**
     * 成员数量
     */
    private Integer memberCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 我的角色
     */
    private String myRole;

}
