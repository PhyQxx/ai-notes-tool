package com.ainotes.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新空间请求DTO
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Data
public class UpdateSpaceRequest {

    /**
     * 空间名称
     */
    @Size(max = 100, message = "空间名称长度不能超过100个字符")
    private String name;

    /**
     * 空间描述
     */
    @Size(max = 500, message = "空间描述长度不能超过500个字符")
    private String description;

}
