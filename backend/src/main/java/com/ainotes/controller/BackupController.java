package com.ainotes.controller;

import com.ainotes.common.result.Result;
import com.ainotes.service.BackupService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 数据备份恢复 Controller
 *
 * @author AI Notes Team
 */
@Slf4j
@RestController
@RequestMapping("/admin/backup")
@RequiredArgsConstructor
public class BackupController {

    private final BackupService backupService;

    /**
     * 导出所有数据为 JSON
     */
    @GetMapping("/export")
    public void exportData(HttpServletResponse response) {
        try {
            response.setContentType("application/json");
            response.setHeader("Content-Disposition", "attachment; filename=ainotes-backup.json");
            backupService.exportData(response.getWriter());
        } catch (Exception e) {
            log.error("导出数据失败", e);
            throw new RuntimeException("导出数据失败: " + e.getMessage());
        }
    }

    /**
     * 导入 JSON 数据恢复
     */
    @PostMapping("/import")
    public Result<Map<String, Object>> importData(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = backupService.importData(file.getInputStream());
            return Result.success("数据恢复成功", result);
        } catch (Exception e) {
            log.error("导入数据失败", e);
            return Result.error("导入数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取备份概览信息
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getBackupInfo() {
        try {
            Map<String, Object> info = backupService.getBackupInfo();
            return Result.success(info);
        } catch (Exception e) {
            log.error("获取备份信息失败", e);
            return Result.error("获取备份信息失败: " + e.getMessage());
        }
    }
}
