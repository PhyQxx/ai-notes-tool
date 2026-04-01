package com.ainotes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建空间请求DTO
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Data
public class CreateSpaceRequest {

    /**
     * 空间名称
     */
    @NotBlank(message = "空间名称不能为空")
    @Size(max = 100, message = "空间名称长度不能超过100个字符")
    private String name;

    /**
     * 空间描述
     */
    @Size(max = 500, message = "空间描述长度不能超过500个字符")
    private String description;

}
