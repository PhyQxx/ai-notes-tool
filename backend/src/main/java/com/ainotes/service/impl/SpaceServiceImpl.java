package com.ainotes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ainotes.common.exception.BusinessException;
import com.ainotes.dto.request.CreateSpaceRequest;
import com.ainotes.dto.request.InviteMemberRequest;
import com.ainotes.dto.request.UpdateMemberRoleRequest;
import com.ainotes.dto.request.UpdateSpaceRequest;
import com.ainotes.dto.response.SpaceMemberResponse;
import com.ainotes.dto.response.SpaceResponse;
import com.ainotes.entity.Space;
import com.ainotes.entity.SpaceMember;
import com.ainotes.entity.User;
import com.ainotes.mapper.SpaceMapper;
import com.ainotes.mapper.SpaceMemberMapper;
import com.ainotes.mapper.UserMapper;
import com.ainotes.service.SpaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 团队空间服务实现类
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpaceServiceImpl implements SpaceService {

    private final SpaceMapper spaceMapper;
    private final SpaceMemberMapper spaceMemberMapper;
    private final UserMapper userMapper;

    /**
     * 角色权限等级（数字越大权限越高）
     */
    private static final int ROLE_LEVEL_OWNER = 100;
    private static final int ROLE_LEVEL_ADMIN = 80;
    private static final int ROLE_LEVEL_EDITOR = 60;
    private static final int ROLE_LEVEL_VIEWER = 40;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSpace(Long userId, CreateSpaceRequest request) {
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 创建空间
        Space space = new Space();
        space.setName(request.getName());
        space.setDescription(request.getDescription());
        space.setOwnerId(userId);
        space.setMemberCount(1);
        spaceMapper.insert(space);

        // 创建者作为owner成员
        SpaceMember member = new SpaceMember();
        member.setSpaceId(space.getId());
        member.setUserId(userId);
        member.setRole("owner");
        member.setStatus(1);
        member.setJoinedAt(LocalDateTime.now());
        spaceMemberMapper.insert(member);

        log.info("创建空间成功，空间ID：{}，创建者：{}", space.getId(), userId);
        return space.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpace(Long userId, Long spaceId, UpdateSpaceRequest request) {
        // 检查空间是否存在
        Space space = spaceMapper.selectById(spaceId);
        if (space == null) {
            throw new BusinessException("空间不存在");
        }

        // 检查权限（仅owner或admin可以更新）
        checkPermission(userId, spaceId, "admin");

        // 更新字段
        if (StringUtils.hasText(request.getName())) {
            space.setName(request.getName());
        }
        if (request.getDescription() != null) {
            space.setDescription(request.getDescription());
        }
        spaceMapper.updateById(space);

        log.info("更新空间成功，空间ID：{}，操作者：{}", spaceId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSpace(Long userId, Long spaceId) {
        // 检查空间是否存在
        Space space = spaceMapper.selectById(spaceId);
        if (space == null) {
            throw new BusinessException("空间不存在");
        }

        // 检查权限（仅owner可以删除）
        if (!space.getOwnerId().equals(userId)) {
            throw new BusinessException("只有空间所有者可以删除空间");
        }

        // 删除空间（级联删除成员、笔记等）
        spaceMapper.deleteById(spaceId);

        log.info("删除空间成功，空间ID：{}，操作者：{}", spaceId, userId);
    }

    @Override
    public List<SpaceResponse> listSpaces(Long userId) {
        // 查询用户参与的空间
        LambdaQueryWrapper<SpaceMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(SpaceMember::getUserId, userId);
        memberWrapper.eq(SpaceMember::getStatus, 1);
        List<SpaceMember> members = spaceMemberMapper.selectList(memberWrapper);

        if (members.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> spaceIds = members.stream()
                .map(SpaceMember::getSpaceId)
                .collect(Collectors.toList());

        // 查询空间信息
        List<Space> spaces = spaceMapper.selectBatchIds(spaceIds);

        // 查询空间所有者信息
        List<Long> ownerIds = spaces.stream()
                .map(Space::getOwnerId)
                .distinct()
                .collect(Collectors.toList());
        List<User> owners = userMapper.selectBatchIds(ownerIds);
        Map<Long, User> ownerMap = owners.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 组装响应数据
        Map<Long, String> roleMap = members.stream()
                .collect(Collectors.toMap(SpaceMember::getSpaceId, SpaceMember::getRole));

        return spaces.stream().map(space -> {
            SpaceResponse response = new SpaceResponse();
            response.setId(space.getId());
            response.setName(space.getName());
            response.setDescription(space.getDescription());
            response.setMemberCount(space.getMemberCount());
            response.setCreatedAt(space.getCreatedAt());
            response.setMyRole(roleMap.get(space.getId()));

            User owner = ownerMap.get(space.getOwnerId());
            if (owner != null) {
                response.setOwnerName(owner.getUsername());
                response.setOwnerNickname(owner.getNickname());
            }

            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public SpaceResponse getSpaceDetail(Long userId, Long spaceId) {
        // 检查空间是否存在
        Space space = spaceMapper.selectById(spaceId);
        if (space == null) {
            throw new BusinessException("空间不存在");
        }

        // 检查用户是否是空间成员
        checkMembership(userId, spaceId);

        // 查询用户在该空间的角色
        LambdaQueryWrapper<SpaceMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(SpaceMember::getSpaceId, spaceId);
        memberWrapper.eq(SpaceMember::getUserId, userId);
        SpaceMember member = spaceMemberMapper.selectOne(memberWrapper);

        // 查询所有者信息
        User owner = userMapper.selectById(space.getOwnerId());

        // 组装响应数据
        SpaceResponse response = new SpaceResponse();
        response.setId(space.getId());
        response.setName(space.getName());
        response.setDescription(space.getDescription());
        response.setMemberCount(space.getMemberCount());
        response.setCreatedAt(space.getCreatedAt());
        response.setMyRole(member.getRole());

        if (owner != null) {
            response.setOwnerName(owner.getUsername());
            response.setOwnerNickname(owner.getNickname());
        }

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inviteMember(Long userId, Long spaceId, InviteMemberRequest request) {
        // 检查空间是否存在
        Space space = spaceMapper.selectById(spaceId);
        if (space == null) {
            throw new BusinessException("空间不存在");
        }

        // 检查权限（仅owner或admin可以邀请成员）
        checkPermission(userId, spaceId, "admin");

        // 根据邮箱查找用户
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getEmail, request.getEmail());
        User targetUser = userMapper.selectOne(userWrapper);

        if (targetUser == null) {
            throw new BusinessException("用户不存在，请先注册");
        }

        // 检查用户是否已经在空间中
        LambdaQueryWrapper<SpaceMember> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(SpaceMember::getSpaceId, spaceId);
        existWrapper.eq(SpaceMember::getUserId, targetUser.getId());
        SpaceMember existMember = spaceMemberMapper.selectOne(existWrapper);

        if (existMember != null) {
            throw new BusinessException("用户已在空间中");
        }

        // 添加成员
        SpaceMember member = new SpaceMember();
        member.setSpaceId(spaceId);
        member.setUserId(targetUser.getId());
        member.setRole(request.getRole());
        member.setStatus(1);
        member.setJoinedAt(LocalDateTime.now());
        spaceMemberMapper.insert(member);

        // 更新成员数量
        space.setMemberCount(space.getMemberCount() + 1);
        spaceMapper.updateById(space);

        log.info("邀请成员成功，空间ID：{}，被邀请用户：{}，角色：{}", spaceId, targetUser.getId(), request.getRole());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMember(Long userId, Long spaceId, Long targetUserId) {
        // 检查空间是否存在
        Space space = spaceMapper.selectById(spaceId);
        if (space == null) {
            throw new BusinessException("空间不存在");
        }

        // 检查权限（仅owner或admin可以移除成员）
        checkPermission(userId, spaceId, "admin");

        // 不能移除自己
        if (userId.equals(targetUserId)) {
            throw new BusinessException("不能移除自己");
        }

        // 查询目标成员
        LambdaQueryWrapper<SpaceMember> targetWrapper = new LambdaQueryWrapper<>();
        targetWrapper.eq(SpaceMember::getSpaceId, spaceId);
        targetWrapper.eq(SpaceMember::getUserId, targetUserId);
        SpaceMember targetMember = spaceMemberMapper.selectOne(targetWrapper);

        if (targetMember == null) {
            throw new BusinessException("成员不存在");
        }

        // 不能移除owner
        if ("owner".equals(targetMember.getRole())) {
            throw new BusinessException("不能移除空间所有者");
        }

        // 删除成员
        spaceMemberMapper.deleteById(targetMember.getId());

        // 更新成员数量
        space.setMemberCount(space.getMemberCount() - 1);
        spaceMapper.updateById(space);

        log.info("移除成员成功，空间ID：{}，被移除用户：{}，操作者：{}", spaceId, targetUserId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMemberRole(Long userId, Long spaceId, Long targetUserId, UpdateMemberRoleRequest request) {
        // 检查空间是否存在
        Space space = spaceMapper.selectById(spaceId);
        if (space == null) {
            throw new BusinessException("空间不存在");
        }

        // 检查权限（仅owner可以修改角色）
        if (!space.getOwnerId().equals(userId)) {
            throw new BusinessException("只有空间所有者可以修改成员角色");
        }

        // 不能修改自己的角色
        if (userId.equals(targetUserId)) {
            throw new BusinessException("不能修改自己的角色");
        }

        // 查询目标成员
        LambdaQueryWrapper<SpaceMember> targetWrapper = new LambdaQueryWrapper<>();
        targetWrapper.eq(SpaceMember::getSpaceId, spaceId);
        targetWrapper.eq(SpaceMember::getUserId, targetUserId);
        SpaceMember targetMember = spaceMemberMapper.selectOne(targetWrapper);

        if (targetMember == null) {
            throw new BusinessException("成员不存在");
        }

        // 不能修改owner的角色
        if ("owner".equals(targetMember.getRole())) {
            throw new BusinessException("不能修改所有者的角色");
        }

        // 更新角色
        targetMember.setRole(request.getRole());
        spaceMemberMapper.updateById(targetMember);

        log.info("修改成员角色成功，空间ID：{}，目标用户：{}，新角色：{}，操作者：{}",
                spaceId, targetUserId, request.getRole(), userId);
    }

    @Override
    public List<SpaceMemberResponse> getMembers(Long userId, Long spaceId) {
        // 检查空间是否存在
        Space space = spaceMapper.selectById(spaceId);
        if (space == null) {
            throw new BusinessException("空间不存在");
        }

        // 检查用户是否是空间成员
        checkMembership(userId, spaceId);

        // 查询所有成员
        LambdaQueryWrapper<SpaceMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(SpaceMember::getSpaceId, spaceId);
        memberWrapper.eq(SpaceMember::getStatus, 1);
        memberWrapper.orderByAsc(SpaceMember::getJoinedAt);
        List<SpaceMember> members = spaceMemberMapper.selectList(memberWrapper);

        // 查询用户信息
        List<Long> userIds = members.stream()
                .map(SpaceMember::getUserId)
                .collect(Collectors.toList());
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 组装响应数据
        return members.stream().map(member -> {
            SpaceMemberResponse response = new SpaceMemberResponse();
            response.setId(member.getId());
            response.setUserId(member.getUserId());
            response.setRole(member.getRole());
            response.setJoinedAt(member.getJoinedAt());

            User user = userMap.get(member.getUserId());
            if (user != null) {
                response.setUsername(user.getUsername());
                response.setNickname(user.getNickname());
                response.setAvatar(user.getAvatar());
            }

            return response;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leaveSpace(Long userId, Long spaceId) {
        // 检查空间是否存在
        Space space = spaceMapper.selectById(spaceId);
        if (space == null) {
            throw new BusinessException("空间不存在");
        }

        // owner不能退出空间
        if (space.getOwnerId().equals(userId)) {
            throw new BusinessException("空间所有者不能退出空间");
        }

        // 查询成员记录
        LambdaQueryWrapper<SpaceMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(SpaceMember::getSpaceId, spaceId);
        memberWrapper.eq(SpaceMember::getUserId, userId);
        SpaceMember member = spaceMemberMapper.selectOne(memberWrapper);

        if (member == null) {
            throw new BusinessException("不是该空间的成员");
        }

        // 删除成员记录
        spaceMemberMapper.deleteById(member.getId());

        // 更新成员数量
        space.setMemberCount(space.getMemberCount() - 1);
        spaceMapper.updateById(space);

        log.info("退出空间成功，空间ID：{}，用户：{}", spaceId, userId);
    }

    /**
     * 检查权限
     *
     * @param userId        用户ID
     * @param spaceId       空间ID
     * @param requiredRole  要求的最低角色
     */
    private void checkPermission(Long userId, Long spaceId, String requiredRole) {
        // 查询用户角色
        LambdaQueryWrapper<SpaceMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(SpaceMember::getSpaceId, spaceId);
        memberWrapper.eq(SpaceMember::getUserId, userId);
        SpaceMember member = spaceMemberMapper.selectOne(memberWrapper);

        if (member == null) {
            throw new BusinessException("不是该空间的成员");
        }

        int userLevel = getRoleLevel(member.getRole());
        int requiredLevel = getRoleLevel(requiredRole);

        if (userLevel < requiredLevel) {
            throw new BusinessException("权限不足");
        }
    }

    /**
     * 检查是否是空间成员
     *
     * @param userId  用户ID
     * @param spaceId 空间ID
     */
    private void checkMembership(Long userId, Long spaceId) {
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
     * 获取角色等级
     *
     * @param role 角色
     * @return 角色等级
     */
    private int getRoleLevel(String role) {
        switch (role) {
            case "owner":
                return ROLE_LEVEL_OWNER;
            case "admin":
                return ROLE_LEVEL_ADMIN;
            case "editor":
                return ROLE_LEVEL_EDITOR;
            case "viewer":
                return ROLE_LEVEL_VIEWER;
            default:
                return 0;
        }
    }

}
