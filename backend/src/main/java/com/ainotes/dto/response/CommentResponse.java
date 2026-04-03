package com.ainotes.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论响应DTO
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Data
public class CommentResponse {

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 笔记ID
     */
    private Long noteId;

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
     * 父评论ID
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
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 子评论列表
     */
    private List<CommentResponse> replies;

}
