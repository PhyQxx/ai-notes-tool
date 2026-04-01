package com.ainotes.common.enums;

import lombok.Getter;

/**
 * 笔记内容类型枚举
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Getter
public enum NoteContentType {

    /**
     * Markdown编辑器
     */
    MARKDOWN("markdown", "Markdown编辑器"),

    /**
     * 富文本编辑器
     */
    RICHTEXT("richtext", "富文本编辑器");

    /**
     * 类型编码
     */
    private final String code;

    /**
     * 类型名称
     */
    private final String name;

    /**
     * 构造方法
     *
     * @param code 类型编码
     * @param name 类型名称
     */
    NoteContentType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 类型编码
     * @return 枚举对象
     */
    public static NoteContentType fromCode(String code) {
        for (NoteContentType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return MARKDOWN; // 默认返回Markdown
    }

}
