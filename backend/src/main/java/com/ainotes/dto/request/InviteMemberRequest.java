package com.ainotes.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 邀请成员请求DTO
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Data
public class InviteMemberRequest {

    /**
     * 邮箱地址
     */
    @NotBlank(message = "邮箱地址不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 角色：admin-管理员，editor-编辑者，viewer-查看者
     */
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(admin|editor|viewer)$", message = "角色必须是admin、editor或viewer")
    private String role;

}
