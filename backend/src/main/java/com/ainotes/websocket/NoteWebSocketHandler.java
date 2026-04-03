package com.ainotes.websocket;

import com.ainotes.util.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket处理器 - 统一处理实时协作和通知推送
 */
@Slf4j
@Component
public class NoteWebSocketHandler extends TextWebSocketHandler {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    /** userId -> WebSocketSession（每个用户一个连接） */
    private final ConcurrentHashMap<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    /** noteId -> Set<userId> 在线编辑用户 */
    private final ConcurrentHashMap<Long, Set<Long>> noteUsers = new ConcurrentHashMap<>();

    /** userId -> noteId 当前正在编辑的笔记 */
    private final ConcurrentHashMap<Long, Long> userNoteMap = new ConcurrentHashMap<>();

    public NoteWebSocketHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserId(session);
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        session.getAttributes().put("userId", userId);
        userSessions.put(userId, session);
        log.info("WebSocket连接建立, userId={}", userId);

        // 发送连接成功消息
        sendMessage(session, Map.of(
                "type", "connected",
                "userId", userId
        ));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = getUserId(session);
        if (userId == null) return;

        try {
            JsonNode json = objectMapper.readTree(message.getPayload());
            String type = json.get("type").asText();

            switch (type) {
                case "join_note" -> handleJoinNote(userId, json);
                case "leave_note" -> handleLeaveNote(userId, json);
                case "cursor_move" -> handleCursorMove(userId, json);
                case "content_change" -> handleContentChange(userId, json);
                default -> log.warn("未知消息类型: {}", type);
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息失败: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = getUserId(session);
        if (userId == null) return;

        userSessions.remove(userId);
        Long noteId = userNoteMap.remove(userId);
        if (noteId != null) {
            Set<Long> users = noteUsers.get(noteId);
            if (users != null) {
                users.remove(userId);
                if (users.isEmpty()) {
                    noteUsers.remove(noteId);
                } else {
                    // 通知其他人该用户离开了
                    broadcastToNote(noteId, userId, Map.of(
                            "type", "user_leave",
                            "userId", userId
                    ));
                }
            }
        }
        log.info("WebSocket连接关闭, userId={}", userId);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket传输错误, session={}: {}", session.getId(), exception.getMessage());
    }

    /**
     * 加入笔记编辑房间
     */
    private void handleJoinNote(Long userId, JsonNode json) {
        Long noteId = json.get("noteId").asLong();
        userNoteMap.put(userId, noteId);
        noteUsers.computeIfAbsent(noteId, k -> ConcurrentHashMap.newKeySet()).add(userId);

        log.info("用户 {} 加入笔记 {} 编辑", userId, noteId);

        // 通知其他人有人加入
        broadcastToNote(noteId, userId, Map.of(
                "type", "user_join",
                "userId", userId,
                "noteId", noteId
        ));

        // 发送当前在线用户列表给新加入的用户
        Set<Long> onlineUsers = noteUsers.getOrDefault(noteId, Set.of());
        sendMessageToUser(userId, Map.of(
                "type", "online_users",
                "noteId", noteId,
                "users", onlineUsers
        ));
    }

    /**
     * 离开笔记编辑房间
     */
    private void handleLeaveNote(Long userId, JsonNode json) {
        Long noteId = json.get("noteId").asLong();
        userNoteMap.remove(userId);
        Set<Long> users = noteUsers.get(noteId);
        if (users != null) {
            users.remove(userId);
            if (users.isEmpty()) {
                noteUsers.remove(noteId);
            }
        }
        broadcastToNote(noteId, userId, Map.of(
                "type", "user_leave",
                "userId", userId,
                "noteId", noteId
        ));
    }

    /**
     * 广播光标位置
     */
    private void handleCursorMove(Long userId, JsonNode json) {
        Long noteId = userNoteMap.get(userId);
        if (noteId == null) return;

        broadcastToNote(noteId, userId, Map.of(
                "type", "cursor_move",
                "userId", userId,
                "noteId", noteId,
                "data", json.get("data")
        ));
    }

    /**
     * 广播内容变更
     */
    private void handleContentChange(Long userId, JsonNode json) {
        Long noteId = userNoteMap.get(userId);
        if (noteId == null) return;

        broadcastToNote(noteId, userId, Map.of(
                "type", "content_change",
                "userId", userId,
                "noteId", noteId,
                "data", json.get("data")
        ));
    }

    /**
     * 广播消息给笔记房间的其他用户
     */
    private void broadcastToNote(Long noteId, Long excludeUserId, Map<String, Object> message) {
        Set<Long> users = noteUsers.get(noteId);
        if (users == null) return;

        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("序列化消息失败", e);
            return;
        }

        for (Long uid : users) {
            if (!uid.equals(excludeUserId)) {
                WebSocketSession session = userSessions.get(uid);
                if (session != null && session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(json));
                    } catch (IOException e) {
                        log.error("发送消息给用户 {} 失败", uid, e);
                    }
                }
            }
        }
    }

    /**
     * 推送通知给指定用户
     */
    public void sendNotification(Long userId, Map<String, Object> notification) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            sendMessageToUser(userId, Map.of(
                    "type", "new_notification",
                    "data", notification
            ));
        }
    }

    /**
     * 获取笔记在线用户数
     */
    public int getOnlineCount(Long noteId) {
        Set<Long> users = noteUsers.get(noteId);
        return users == null ? 0 : users.size();
    }

    /**
     * 发送消息给指定用户
     */
    private void sendMessageToUser(Long userId, Map<String, Object> message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            sendMessage(session, message);
        }
    }

    private void sendMessage(WebSocketSession session, Map<String, Object> message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            log.error("发送WebSocket消息失败", e);
        }
    }

    private Long getUserId(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        if (userId != null) return (Long) userId;

        // 从query param获取token并验证
        String query = session.getUri().getQuery();
        if (query != null) {
            String token = UriComponentsBuilder.newInstance()
                    .query(query)
                    .build()
                    .getQueryParams()
                    .getFirst("token");
            if (token != null && jwtUtil.validateToken(token)) {
                return jwtUtil.getUserIdFromToken(token);
            }
        }
        return null;
    }
}
