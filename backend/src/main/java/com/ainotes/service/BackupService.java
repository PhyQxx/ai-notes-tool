package com.ainotes.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Map;

/**
 * 数据备份恢复服务接口
 *
 * @author AI Notes Team
 */
public interface BackupService {

    /**
     * 导出所有数据为 JSON
     */
    void exportData(Writer writer) throws IOException;

    /**
     * 导入 JSON 数据恢复
     */
    Map<String, Object> importData(InputStream inputStream) throws IOException;

    /**
     * 获取备份概览信息
     */
    Map<String, Object> getBackupInfo();
}
