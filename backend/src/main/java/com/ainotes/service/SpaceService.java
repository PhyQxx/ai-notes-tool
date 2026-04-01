package com.ainotes.service;

import com.ainotes.dto.request.CreateSpaceRequest;
import com.ainotes.dto.request.InviteMemberRequest;
import com.ainotes.dto.request.UpdateMemberRoleRequest;
import com.ainotes.dto.request.UpdateSpaceRequest;
import com.ainotes.dto.response.SpaceMemberResponse;
import com.ainotes.dto.response.SpaceResponse;

import java.util.List;

/**
 * 团队空间服务接口
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
public interface SpaceService {

    /**
     * 创建空间
     *
     * @param userId  用户ID
     * @param request 创建请求
     * @return 空间ID
     */
    Long createSpace(Long userId, CreateSpaceRequest request);

    /**
     * 更新空间
     *
     * @param userId  用户ID
     * @param spaceId 空间ID
     * @param request 更新请求
     */
    void updateSpace(Long userId, Long spaceId, UpdateSpaceRequest request);

    /**
     * 删除空间
     *
     * @param userId  用户ID
     * @param spaceId 空间ID
     */
    void deleteSpace(Long userId, Long spaceId);

    /**
     * 获取我参与的空间列表
     *
     * @param userId 用户ID
     * @return 空间列表
     */
    List<SpaceResponse> listSpaces(Long userId);

    /**
     * 获取空间详情
     *
     * @param userId  用户ID
     * @param spaceId 空间ID
     * @return 空间详情
     */
    SpaceResponse getSpaceDetail(Long userId, Long spaceId);

    /**
     * 邀请成员
     *
     * @param userId  用户ID
     * @param spaceId 空间ID
     * @param request 邀请请求
     */
    void inviteMember(Long userId, Long spaceId, InviteMemberRequest request);

    /**
     * 移除成员
     *
     * @param userId       用户ID
     * @param spaceId      空间ID
     * @param targetUserId 目标用户ID
     */
    void removeMember(Long userId, Long spaceId, Long targetUserId);

    /**
     * 修改成员角色
     *
     * @param userId       用户ID
     * @param spaceId      空间ID
     * @param targetUserId 目标用户ID
     * @param request      修改角色请求
     */
    void updateMemberRole(Long userId, Long spaceId, Long targetUserId, UpdateMemberRoleRequest request);

    /**
     * 获取成员列表
     *
     * @param userId  用户ID
     * @param spaceId 空间ID
     * @return 成员列表
     */
    List<SpaceMemberResponse> getMembers(Long userId, Long spaceId);

    /**
     * 退出空间
     *
     * @param userId  用户ID
     * @param spaceId 空间ID
     */
    void leaveSpace(Long userId, Long spaceId);

}
