package com.ainotes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建文件夹请求DTO
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Data
public class CreateFolderRequest {

    /**
     * 文件夹名称
     */
    @NotBlank(message = "文件夹名称不能为空")
    @Size(max = 100, message = "文件夹名称长度不能超过100个字符")
    private String name;

    /**
     * 父文件夹ID，0表示根目录
     */
    private Long parentId;

}
