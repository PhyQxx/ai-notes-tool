package com.ainotes.controller;

import com.ainotes.dto.request.*;
import com.ainotes.dto.response.SpaceMemberResponse;
import com.ainotes.dto.response.SpaceResponse;
import com.ainotes.service.SpaceService;
import com.ainotes.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 团队空间控制器
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/spaces")
@RequiredArgsConstructor
@Validated
@Tag(name = "团队空间管理", description = "团队空间的创建、编辑、成员管理、权限控制等操作")
public class SpaceController {

    private final SpaceService spaceService;

    /**
     * 获取我的空间列表
     *
     * @return 空间列表
     */
    @GetMapping
    @Operation(summary = "获取我的空间列表")
    public Result<List<SpaceResponse>> listSpaces(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<SpaceResponse> spaces = spaceService.listSpaces(userId);
        return Result.success(spaces);
    }

    /**
     * 创建空间
     *
     * @param request 创建请求
     * @return 空间ID
     */
    @PostMapping
    @Operation(summary = "创建空间")
    public Result<Long> createSpace(@Valid @RequestBody CreateSpaceRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Long spaceId = spaceService.createSpace(userId, request);
        return Result.success("创建成功", spaceId);
    }

    /**
     * 获取空间详情
     *
     * @param id 空间ID
     * @return 空间详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取空间详情")
    public Result<SpaceResponse> getSpaceDetail(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        SpaceResponse space = spaceService.getSpaceDetail(userId, id);
        return Result.success(space);
    }

    /**
     * 更新空间
     *
     * @param id      空间ID
     * @param request 更新请求
     * @return 成功信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新空间")
    public Result<Void> updateSpace(@PathVariable Long id, @Valid @RequestBody UpdateSpaceRequest request,
                                    Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        spaceService.updateSpace(userId, id, request);
        return Result.success("更新成功", null);
    }

    /**
     * 删除空间
     *
     * @param id 空间ID
     * @return 成功信息
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除空间")
    public Result<Void> deleteSpace(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        spaceService.deleteSpace(userId, id);
        return Result.success("删除成功", null);
    }

    /**
     * 获取成员列表
     *
     * @param id 空间ID
     * @return 成员列表
     */
    @GetMapping("/{id}/members")
    @Operation(summary = "获取成员列表")
    public Result<List<SpaceMemberResponse>> getMembers(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<SpaceMemberResponse> members = spaceService.getMembers(userId, id);
        return Result.success(members);
    }

    /**
     * 邀请成员
     *
     * @param id      空间ID
     * @param request 邀请请求
     * @return 成功信息
     */
    @PostMapping("/{id}/members")
    @Operation(summary = "邀请成员")
    public Result<Void> inviteMember(@PathVariable Long id, @Valid @RequestBody InviteMemberRequest request,
                                     Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        spaceService.inviteMember(userId, id, request);
        return Result.success("邀请成功", null);
    }

    /**
     * 移除成员
     *
     * @param id       空间ID
     * @param userId   目标用户ID
     * @return 成功信息
     */
    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "移除成员")
    public Result<Void> removeMember(@PathVariable Long id, @PathVariable Long userId,
                                      Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        spaceService.removeMember(currentUserId, id, userId);
        return Result.success("移除成功", null);
    }

    /**
     * 修改成员角色
     *
     * @param id       空间ID
     * @param userId   目标用户ID
     * @param request  修改角色请求
     * @return 成功信息
     */
    @PutMapping("/{id}/members/{userId}/role")
    @Operation(summary = "修改成员角色")
    public Result<Void> updateMemberRole(@PathVariable Long id, @PathVariable Long userId,
                                         @Valid @RequestBody UpdateMemberRoleRequest request,
                                         Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        spaceService.updateMemberRole(currentUserId, id, userId, request);
        return Result.success("修改成功", null);
    }

    /**
     * 退出空间
     *
     * @param id 空间ID
     * @return 成功信息
     */
    @PostMapping("/{id}/leave")
    @Operation(summary = "退出空间")
    public Result<Void> leaveSpace(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        spaceService.leaveSpace(userId, id);
        return Result.success("退出成功", null);
    }

}
