package com.ainotes.service;

import com.ainotes.entity.Notification;

import java.util.List;

public interface NotificationService {

    /**
     * 创建通知
     */
    Notification create(Notification notification);

    /**
     * 获取通知列表（未读优先）
     */
    List<Notification> listByUserId(Long userId, int page, int size);

    /**
     * 获取未读数量
     */
    long getUnreadCount(Long userId);

    /**
     * 标记已读
     */
    void markAsRead(Long id, Long userId);

    /**
     * 发送通知
     */
    void sendNotification(Long userId, String title, String content, String type, String link);

    /**
     * 全部标记已读
     */
    void markAllRead(Long userId);
}
