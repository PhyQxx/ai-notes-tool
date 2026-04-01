package com.ainotes.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建版本请求DTO
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Data
public class CreateVersionRequest {

    /**
     * 版本备注
     */
    @Size(max = 500, message = "版本备注不能超过500个字符")
    private String remark;

}
