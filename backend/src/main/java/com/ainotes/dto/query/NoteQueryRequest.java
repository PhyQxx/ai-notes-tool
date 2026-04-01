package com.ainotes.dto.query;

import lombok.Data;

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
     * 页码，从1开始
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;

}
