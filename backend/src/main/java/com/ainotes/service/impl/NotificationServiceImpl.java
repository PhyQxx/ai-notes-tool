package com.ainotes.service.impl;

import com.ainotes.entity.Notification;
import com.ainotes.mapper.NotificationMapper;
import com.ainotes.service.NotificationService;
import com.ainotes.websocket.NoteWebSocketHandler;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final NoteWebSocketHandler webSocketHandler;

    @Override
    public Notification create(Notification notification) {
        notification.setIsRead(false);
        notificationMapper.insert(notification);

        // WebSocket实时推送
        Map<String, Object> data = new HashMap<>();
        data.put("id", notification.getId());
        data.put("type", notification.getType());
        data.put("title", notification.getTitle());
        data.put("content", notification.getContent());
        data.put("link", notification.getLink());
        data.put("createdAt", notification.getCreatedAt());
        if (notification.getSenderId() != null) {
            data.put("senderId", notification.getSenderId());
        }
        webSocketHandler.sendNotification(notification.getUserId(), data);

        return notification;
    }

    @Override
    public List<Notification> listByUserId(Long userId, int page, int size) {
        // 先查未读，再查已读
        Page<Notification> unreadPage = notificationMapper.selectPage(
                new Page<>(1, (long) size),
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, false)
                        .orderByDesc(Notification::getCreatedAt)
        );

        int remaining = size - (int) unreadPage.getRecords().size();
        if (remaining > 0) {
            Page<Notification> readPage = notificationMapper.selectPage(
                    new Page<>(1, (long) remaining),
                    new LambdaQueryWrapper<Notification>()
                            .eq(Notification::getUserId, userId)
                            .eq(Notification::getIsRead, true)
                            .orderByDesc(Notification::getCreatedAt)
            );
            unreadPage.getRecords().addAll(readPage.getRecords());
        }

        return unreadPage.getRecords();
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, false)
        );
    }

    @Override
    public void markAsRead(Long id, Long userId) {
        notificationMapper.update(null,
                new LambdaUpdateWrapper<Notification>()
                        .eq(Notification::getId, id)
                        .eq(Notification::getUserId, userId)
                        .set(Notification::getIsRead, true)
        );
    }

    @Override
    public void markAllRead(Long userId) {
        notificationMapper.update(null,
                new LambdaUpdateWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .set(Notification::getIsRead, true)
        );
    }
}
