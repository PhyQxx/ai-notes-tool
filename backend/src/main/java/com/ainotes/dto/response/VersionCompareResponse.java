package com.ainotes.dto.response;

import lombok.Data;

/**
 * 版本对比响应DTO
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Data
public class VersionCompareResponse {

    /**
     * 版本1
     */
    private VersionInfo version1;

    /**
     * 版本2
     */
    private VersionInfo version2;

    /**
     * 版本信息
     */
    @Data
    public static class VersionInfo {

        /**
         * 版本ID
         */
        private Long id;

        /**
         * 版本号
         */
        private Integer versionNo;

        /**
         * 标题
         */
        private String title;

        /**
         * 内容
         */
        private String content;

        /**
         * 备注
         */
        private String remark;

        /**
         * 创建时间
         */
        private String createdAt;

        /**
         * 创建人ID
         */
        private Long createdBy;

    }

}
