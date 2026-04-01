package com.ainotes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ainotes.common.exception.BusinessException;
import com.ainotes.dto.request.CreateCommentRequest;
import com.ainotes.dto.response.CommentResponse;
import com.ainotes.entity.Comment;
import com.ainotes.entity.Note;
import com.ainotes.entity.SpaceMember;
import com.ainotes.entity.User;
import com.ainotes.mapper.CommentMapper;
import com.ainotes.mapper.NoteMapper;
import com.ainotes.mapper.SpaceMemberMapper;
import com.ainotes.mapper.UserMapper;
import com.ainotes.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final NoteMapper noteMapper;
    private final SpaceMemberMapper spaceMemberMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createComment(Long userId, CreateCommentRequest request) {
        // 查询笔记
        Note note = noteMapper.selectById(request.getNoteId());
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }

        // 检查笔记状态
        if (note.getStatus() == 0) {
            throw new BusinessException("笔记已被删除");
        }

        // 如果笔记属于某个空间，检查用户是否有该空间权限
        if (note.getSpaceId() != null) {
            checkSpacePermission(userId, note.getSpaceId());
        } else {
            // 个人笔记，只能自己评论
            if (!note.getUserId().equals(userId)) {
                throw new BusinessException("无权限评论该笔记");
            }
        }

        // 如果是回复评论，检查父评论是否存在
        if (request.getParentId() != null && request.getParentId() > 0) {
            Comment parentComment = commentMapper.selectById(request.getParentId());
            if (parentComment == null) {
                throw new BusinessException("父评论不存在");
            }
            // 父评论必须属于同一笔记
            if (!parentComment.getNoteId().equals(request.getNoteId())) {
                throw new BusinessException("父评论不属于该笔记");
            }
        }

        // 创建评论
        Comment comment = new Comment();
        comment.setNoteId(request.getNoteId());
        comment.setUserId(userId);
        comment.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        comment.setContent(request.getContent());
        comment.setStatus(1);
        commentMapper.insert(comment);

        log.info("创建评论成功，评论ID：{}，笔记ID：{}，用户：{}", comment.getId(), request.getNoteId(), userId);
        return comment.getId();
    }

    @Override
    public List<CommentResponse> listComments(Long userId, Long noteId) {
        // 查询笔记
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }

        // 检查笔记状态
        if (note.getStatus() == 0) {
            throw new BusinessException("笔记已被删除");
        }

        // 如果笔记属于某个空间，检查用户是否有该空间权限
        if (note.getSpaceId() != null) {
            checkSpacePermission(userId, note.getSpaceId());
        } else {
            // 个人笔记，只能自己查看
            if (!note.getUserId().equals(userId)) {
                throw new BusinessException("无权限查看该笔记的评论");
            }
        }

        // 查询所有评论
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(Comment::getNoteId, noteId);
        commentWrapper.eq(Comment::getStatus, 1);
        commentWrapper.orderByAsc(Comment::getCreatedAt);
        List<Comment> allComments = commentMapper.selectList(commentWrapper);

        if (allComments.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询用户信息
        List<Long> userIds = allComments.stream()
                .map(Comment::getUserId)
                .distinct()
                .collect(Collectors.toList());

        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, String> usernameMap = users.stream().collect(Collectors.toMap(User::getId, User::getUsername));
        Map<Long, String> nicknameMap = users.stream().collect(Collectors.toMap(User::getId, User::getNickname));
        Map<Long, String> avatarMap = users.stream().collect(Collectors.toMap(User::getId, User::getAvatar));

        // 构建树形结构
        Map<Long, CommentResponse> commentMap = allComments.stream()
                .collect(Collectors.toMap(Comment::getId, comment -> {
                    CommentResponse response = new CommentResponse();
                    response.setId(comment.getId());
                    response.setNoteId(comment.getNoteId());
                    response.setUserId(comment.getUserId());
                    response.setParentId(comment.getParentId());
                    response.setContent(comment.getContent());
                    response.setCreatedAt(comment.getCreatedAt());

                    response.setUsername(usernameMap.get(comment.getUserId()));
                    response.setNickname(nicknameMap.get(comment.getUserId()));
                    response.setAvatar(avatarMap.get(comment.getUserId()));

                    return response;
                }));

        List<CommentResponse> rootComments = new ArrayList<>();
        for (Comment comment : allComments) {
            CommentResponse response = commentMap.get(comment.getId());
            if (comment.getParentId() == 0 || comment.getParentId() == null) {
                // 顶级评论
                rootComments.add(response);
            } else {
                // 子评论
                CommentResponse parentResponse = commentMap.get(comment.getParentId());
                if (parentResponse != null) {
                    if (parentResponse.getReplies() == null) {
                        parentResponse.setReplies(new ArrayList<>());
                    }
                    parentResponse.getReplies().add(response);
                }
            }
        }

        return rootComments;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long userId, Long commentId) {
        // 查询评论
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        // 检查状态
        if (comment.getStatus() == 0) {
            throw new BusinessException("评论已被删除");
        }

        // 查询笔记
        Note note = noteMapper.selectById(comment.getNoteId());
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }

        // 检查权限：评论作者或空间管理员可以删除
        boolean isAuthor = comment.getUserId().equals(userId);
        boolean canManage = false;

        // 如果笔记属于某个空间，检查用户是否是空间管理员
        if (note.getSpaceId() != null) {
            canManage = checkSpaceAdminPermission(userId, note.getSpaceId());
        } else {
            // 个人笔记，只有作者可以删除评论
            canManage = note.getUserId().equals(userId);
        }

        if (!isAuthor && !canManage) {
            throw new BusinessException("无权限删除该评论");
        }

        // 软删除评论
        comment.setStatus(0);
        commentMapper.updateById(comment);

        log.info("删除评论成功，评论ID：{}，操作者：{}", commentId, userId);
    }

    /**
     * 检查空间权限（viewer及以上）
     *
     * @param userId  用户ID
     * @param spaceId 空间ID
     */
    private void checkSpacePermission(Long userId, Long spaceId) {
        LambdaQueryWrapper<SpaceMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(SpaceMember::getSpaceId, spaceId);
        memberWrapper.eq(SpaceMember::getUserId, userId);
        SpaceMember member = spaceMemberMapper.selectOne(memberWrapper);

        if (member == null) {
            throw new BusinessException("不是该空间的成员");
        }

        if (member.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
    }

    /**
     * 检查空间管理员权限
     *
     * @param userId  用户ID
     * @param spaceId 空间ID
     * @return 是否有管理权限
     */
    private boolean checkSpaceAdminPermission(Long userId, Long spaceId) {
        LambdaQueryWrapper<SpaceMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(SpaceMember::getSpaceId, spaceId);
        memberWrapper.eq(SpaceMember::getUserId, userId);
        SpaceMember member = spaceMemberMapper.selectOne(memberWrapper);

        if (member == null || member.getStatus() == 0) {
            return false;
        }

        return "owner".equals(member.getRole()) || "admin".equals(member.getRole());
    }

}
