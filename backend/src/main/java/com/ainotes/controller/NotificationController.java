package com.ainotes.controller;

import com.ainotes.common.result.Result;
import com.ainotes.entity.Notification;
import com.ainotes.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public Result<Notification> create(@RequestBody Notification notification) {
        return Result.success(notificationService.create(notification));
    }

    @GetMapping
    public Result<List<Notification>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(notificationService.listByUserId(userId, page, size));
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        notificationService.markAsRead(id, userId);
        return Result.success();
    }

    @PutMapping("/read-all")
    public Result<Void> markAllRead(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        notificationService.markAllRead(userId);
        return Result.success();
    }
}
