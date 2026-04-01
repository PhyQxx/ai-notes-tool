package com.ainotes.common.enums;

import lombok.Getter;

/**
 * 空间角色枚举
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Getter
public enum SpaceRole {

    /**
     * 所有者
     */
    OWNER("owner", "所有者"),

    /**
     * 管理员
     */
    ADMIN("admin", "管理员"),

    /**
     * 编辑者
     */
    EDITOR("editor", "编辑者"),

    /**
     * 查看者
     */
    VIEWER("viewer", "查看者");

    /**
     * 角色编码
     */
    private final String code;

    /**
     * 角色名称
     */
    private final String name;

    /**
     * 构造方法
     *
     * @param code 角色编码
     * @param name 角色名称
     */
    SpaceRole(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 角色编码
     * @return 枚举对象
     */
    public static SpaceRole fromCode(String code) {
        for (SpaceRole role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return VIEWER; // 默认返回查看者
    }

}
