package com.ainotes.controller;

import com.ainotes.common.result.Result;
import com.ainotes.entity.Canvas;
import com.ainotes.service.CanvasService;
import com.ainotes.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/canvas")
@RequiredArgsConstructor
@Tag(name = "知识画布", description = "知识卡片自由排列与连线")
public class CanvasController {

    private final CanvasService canvasService;

    @GetMapping
    @Operation(summary = "获取用户的画布列表")
    public Result<List<Canvas>> listCanvases() {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(canvasService.listUserCanvases(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取画布详情")
    public Result<Canvas> getCanvas(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        Canvas canvas = canvasService.getById(id);
        if (canvas != null && canvas.getUserId().equals(userId)) {
            return Result.success(canvas);
        }
        return Result.error("画布不存在或无权限访问");
    }

    @PostMapping
    @Operation(summary = "创建新画布")
    public Result<Canvas> createCanvas(@RequestBody Map<String, String> body) {
        Long userId = SecurityUtil.getCurrentUserId();
        String title = body.getOrDefault("title", "未命名画布");
        return Result.success(canvasService.createCanvas(userId, title));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新画布内容")
    public Result<Void> updateCanvas(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long userId = SecurityUtil.getCurrentUserId();
        String data = body.get("data");
        canvasService.updateCanvasData(userId, id, data);
        return Result.success("已保存", null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除画布")
    public Result<Void> deleteCanvas(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        Canvas canvas = canvasService.getById(id);
        if (canvas != null && canvas.getUserId().equals(userId)) {
            canvasService.removeById(id);
            return Result.success("已删除", null);
        }
        return Result.error("删除失败");
    }
}
