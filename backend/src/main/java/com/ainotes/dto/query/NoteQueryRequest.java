package com.ainotes.dto.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记查询请求DTO
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Data
public class NoteQueryRequest {

    /**
     * 关键词（搜索标题和内容）
     */
    private String keyword;

    /**
     * 文件夹ID
     */
    private Long folderId;

    /**
     * 标签
     */
    private String tag;

    /**
     * 是否收藏：1-是，0-否
     */
    private Integer isFavorite;

    /**
     * 是否置顶：1-是，0-否
     */
    private Integer isTop;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 排序字段：createdAt-创建时间，updatedAt-更新时间，viewCount-查看次数
     */
    private String sortBy = "updatedAt";

    /**
     * 排序方向：asc-升序，desc-降序
     */
    private String sortOrder = "desc";

    /**
     * 页码，从1开始
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;

}
