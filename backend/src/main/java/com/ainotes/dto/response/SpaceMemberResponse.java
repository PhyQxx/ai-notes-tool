package com.ainotes.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 空间成员响应DTO
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Data
public class SpaceMemberResponse {

    /**
     * 成员ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 角色
     */
    private String role;

    /**
     * 加入时间
     */
    private LocalDateTime joinedAt;

}
