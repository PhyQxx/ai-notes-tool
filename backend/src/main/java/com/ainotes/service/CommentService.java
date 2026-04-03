package com.ainotes.service;

import com.ainotes.dto.request.CreateCommentRequest;
import com.ainotes.dto.response.CommentResponse;

import java.util.List;

/**
 * 评论服务接口
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
public interface CommentService {

    /**
     * 创建评论
     *
     * @param userId  用户ID
     * @param request 创建评论请求
     * @return 评论ID
     */
    Long createComment(Long userId, CreateCommentRequest request);

    /**
     * 获取笔记评论（树形结构）
     *
     * @param userId  用户ID
     * @param noteId  笔记ID
     * @return 评论列表
     */
    List<CommentResponse> listComments(Long userId, Long noteId);

    /**
     * 删除评论
     *
     * @param userId    用户ID
     * @param commentId 评论ID
     */
    void deleteComment(Long userId, Long commentId);

    void toggleStatus(Long userId, Long commentId, String status);

}
