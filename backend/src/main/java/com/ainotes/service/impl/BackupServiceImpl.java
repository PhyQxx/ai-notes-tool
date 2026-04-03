package com.ainotes.service.impl;

import com.ainotes.entity.*;
import com.ainotes.mapper.*;
import com.ainotes.service.BackupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.*;

/**
 * 数据备份恢复服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BackupServiceImpl implements BackupService {

    private final NoteMapper noteMapper;
    private final FolderMapper folderMapper;
    private final SpaceMapper spaceMapper;
    private final SpaceMemberMapper spaceMemberMapper;
    private final CommentMapper commentMapper;
    private final AttachmentMapper attachmentMapper;
    private final NoteVersionMapper noteVersionMapper;
    private final AIConversationMapper aiConversationMapper;
    private final AIChatMessageMapper aiChatMessageMapper;
    private final NotificationMapper notificationMapper;

    private final ObjectMapper objectMapper;

    public BackupServiceImpl(NoteMapper noteMapper, FolderMapper folderMapper, SpaceMapper spaceMapper,
                            SpaceMemberMapper spaceMemberMapper, CommentMapper commentMapper,
                            AttachmentMapper attachmentMapper, NoteVersionMapper noteVersionMapper,
                            AIConversationMapper aiConversationMapper, AIChatMessageMapper aiChatMessageMapper,
                            NotificationMapper notificationMapper) {
        this.noteMapper = noteMapper;
        this.folderMapper = folderMapper;
        this.spaceMapper = spaceMapper;
        this.spaceMemberMapper = spaceMemberMapper;
        this.commentMapper = commentMapper;
        this.attachmentMapper = attachmentMapper;
        this.noteVersionMapper = noteVersionMapper;
        this.aiConversationMapper = aiConversationMapper;
        this.aiChatMessageMapper = aiChatMessageMapper;
        this.notificationMapper = notificationMapper;
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper = om;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void exportData(Writer writer) throws IOException {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("version", "1.0");
        data.put("exportTime", new Date());
        data.put("notes", noteMapper.selectList(null));
        data.put("folders", folderMapper.selectList(null));
        data.put("spaces", spaceMapper.selectList(null));
        data.put("spaceMembers", spaceMemberMapper.selectList(null));
        data.put("comments", commentMapper.selectList(null));
        data.put("attachments", attachmentMapper.selectList(null));
        data.put("noteVersions", noteVersionMapper.selectList(null));
        data.put("aiConversations", aiConversationMapper.selectList(null));
        data.put("aiChatMessages", aiChatMessageMapper.selectList(null));
        data.put("notifications", notificationMapper.selectList(null));
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, data);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> importData(InputStream inputStream) throws IOException {
        Map<String, Object> data = objectMapper.readValue(inputStream, Map.class);
        String version = (String) data.getOrDefault("version", "unknown");

        int totalImported = 0;

        totalImported += importList(data, "folders", folderMapper, Folder.class);
        totalImported += importList(data, "spaces", spaceMapper, Space.class);
        totalImported += importList(data, "spaceMembers", spaceMemberMapper, SpaceMember.class);
        totalImported += importList(data, "notes", noteMapper, Note.class);
        totalImported += importList(data, "comments", commentMapper, Comment.class);
        totalImported += importList(data, "attachments", attachmentMapper, Attachment.class);
        totalImported += importList(data, "noteVersions", noteVersionMapper, NoteVersion.class);
        totalImported += importList(data, "aiConversations", aiConversationMapper, AIConversation.class);
        totalImported += importList(data, "aiChatMessages", aiChatMessageMapper, AIChatMessage.class);
        totalImported += importList(data, "notifications", notificationMapper, Notification.class);

        Map<String, Object> result = new HashMap<>();
        result.put("version", version);
        result.put("totalImported", totalImported);
        return result;
    }

    @SuppressWarnings("unchecked")
    private int importList(Map<String, Object> data, String key, com.baomidou.mybatisplus.core.mapper.BaseMapper mapper, Class<?> entityClass) throws IOException {
        List<Object> items = (List<Object>) data.get(key);
        if (items == null || items.isEmpty()) return 0;
        for (Object item : items) {
            String json = objectMapper.writeValueAsString(item);
            Object entity = objectMapper.readValue(json, entityClass);
            try {
                mapper.insert(entity);
            } catch (Exception e) {
                log.debug("跳过已存在记录: {}", key);
            }
        }
        return items.size();
    }

    @Override
    public Map<String, Object> getBackupInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("notes", noteMapper.selectCount(null));
        info.put("folders", folderMapper.selectCount(null));
        info.put("spaces", spaceMapper.selectCount(null));
        info.put("comments", commentMapper.selectCount(null));
        info.put("attachments", attachmentMapper.selectCount(null));
        info.put("noteVersions", noteVersionMapper.selectCount(null));
        info.put("aiConversations", aiConversationMapper.selectCount(null));
        return info;
    }
}
