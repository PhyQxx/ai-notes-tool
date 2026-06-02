package com.ainotes.task;

import com.ainotes.service.BackupService;
import com.ainotes.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 自动化备份任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BackupTask {

    private final BackupService backupService;
    private final FileService fileService;

    /**
     * 每天凌晨 3 点执行全量备份
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void autoBackup() {
        log.info("开始执行自动化全量备份...");
        try {
            StringWriter writer = new StringWriter();
            backupService.exportData(writer);
            String json = writer.toString();
            byte[] data = json.getBytes(StandardCharsets.UTF_8);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "backups/ai_notes_backup_" + timestamp + ".json";

            // 上传到 MinIO
            String url = fileService.uploadFile(data, fileName, "application/json");
            log.info("自动化备份完成, 备份文件: {}, URL: {}", fileName, url);
            
            // TODO: 清理旧备份（保留最近 7 天）
        } catch (Exception e) {
            log.error("自动化备份失败: {}", e.getMessage());
        }
    }
}
