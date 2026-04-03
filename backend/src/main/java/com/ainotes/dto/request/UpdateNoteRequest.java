package com.ainotes.dto.request;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新笔记请求DTO
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Data
public class UpdateNoteRequest {

    /**
     * 标题
     */
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
     * 标签(数组或逗号分隔字符串，兼容前端)
     */
    private Object tags;

}
