package com.ainotes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建笔记请求DTO
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Data
public class CreateNoteRequest {

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 内容类型：markdown-Markdown编辑器，richtext-富文本编辑器
     */
    private String contentType;

    /**
     * 文件夹ID
     */
    private Long folderId;

    /**
     * 标签(逗号分隔)
     */
    private String tags;

}
