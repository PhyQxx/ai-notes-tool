package com.ainotes.service;

import org.springframework.http.ResponseEntity;

/**
 * 导出服务接口
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
public interface ExportService {

    /**
     * 导出Markdown文件
     *
     * @param userId  用户ID
     * @param noteId  笔记ID
     * @return 文件响应
     */
    ResponseEntity<byte[]> exportMarkdown(Long userId, Long noteId);

    /**
     * 导出PDF文件
     *
     * @param userId  用户ID
     * @param noteId  笔记ID
     * @return 文件响应
     */
    ResponseEntity<byte[]> exportPDF(Long userId, Long noteId);

    /**
     * 导出Word文件
     *
     * @param userId  用户ID
     * @param noteId  笔记ID
     * @return 文件响应
     */
    ResponseEntity<byte[]> exportWord(Long userId, Long noteId);

}
