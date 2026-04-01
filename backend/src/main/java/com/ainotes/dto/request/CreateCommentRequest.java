package com.ainotes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建评论请求DTO
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Data
public class CreateCommentRequest {

    /**
     * 笔记ID
     */
    @NotNull(message = "笔记ID不能为空")
    private Long noteId;

    /**
     * 父评论ID(0表示顶级评论)
     */
    private Long parentId;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    private String content;

}
