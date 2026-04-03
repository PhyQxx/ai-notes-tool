package com.ainotes.service;

import com.ainotes.entity.*;
import com.ainotes.mapper.*;
import com.ainotes.service.BackupService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.Writer;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据备份恢复服务
 *
 * @author AI Notes Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BackupServiceImpl implements BackupService {

    private final NoteMapper noteMapper;
    private final FolderMapper folderMapper;
    private final CommentMapper commentMapper;
    private final NoteVersionMapper noteVersionMapper;
    private final AIConversationMapper aiConversationMapper;
    private final AIChatMessageMapper aiChatMessageMapper;
    private final UserMapper userMapper;

    private final ObjectMapper objectMapper;

    @Override
    public void exportData(Writer writer) throws IOException {
        ObjectMapper exportMapper = new ObjectMapper();
        exportMapper.registerModule(new JavaTimeModule());
        exportMapper.enable(SerializationFeature.INDENT_OUTPUT);

        Map<String, Object> backup = new LinkedHashMap<>();
        backup.put("version", "1.0");
        backup.put("exportedAt", new Date().toString());

        // 导出用户数据（密码脱敏）
        List<User> users = userMapper.selectList(null);
        users.forEach(u -> u.setPassword(null));
        backup.put("users", users);

        // 导出文件夹
        backup.put("folders", folderMapper.selectList(null));

        // 导出笔记
        backup.put("notes", noteMapper.selectList(null));

        // 导出评论
        backup.put("comments", commentMapper.selectList(null));

        // 导出版本
        backup.put("noteVersions", noteVersionMapper.selectList(null));

        // 导出AI对话
        backup.put("aiConversations", aiConversationMapper.selectList(null));
        backup.put("aiChatMessages", aiChatMessageMapper.selectList(null));

        exportMapper.writeValue(writer, backup);
        writer.flush();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public Map<String, Object> importData(InputStream inputStream) throws IOException {
        ObjectMapper importMapper = new ObjectMapper();
        importMapper.registerModule(new JavaTimeModule());

        Map<String, Object> backup = importMapper.readValue(inputStream, Map.class);
        Map<String, Object> result = new LinkedHashMap<>();

        // 恢复文件夹
        List<Map<String, Object>> folders = (List<Map<String, Object>>) backup.get("folders");
        int folderCount = 0;
        if (folders != null) {
            for (Map<String, Object> f : folders) {
                Folder folder = importMapper.convertValue(f, Folder.class);
                folder.setId(null); // 让数据库自动生成ID
                // 重建父子关系映射
                folderMapper.insert(folder);
                folderCount++;
            }
        }
        result.put("folders", folderCount);

        // 恢复笔记
        List<Map<String, Object>> notes = (List<Map<String, Object>>) backup.get("notes");
        int noteCount = 0;
        if (notes != null) {
            for (Map<String, Object> n : notes) {
                Note note = importMapper.convertValue(n, Note.class);
                note.setId(null);
                note.setStatus(1); // 确保状态正常
                noteMapper.insert(note);
                noteCount++;
            }
        }
        result.put("notes", noteCount);

        // 恢复评论
        List<Map<String, Object>> comments = (List<Map<String, Object>>) backup.get("comments");
        int commentCount = 0;
        if (comments != null) {
            for (Map<String, Object> c : comments) {
                Comment comment = importMapper.convertValue(c, Comment.class);
                comment.setId(null);
                commentMapper.insert(comment);
                commentCount++;
            }
        }
        result.put("comments", commentCount);

        // 恢复版本
        List<Map<String, Object>> versions = (List<Map<String, Object>>) backup.get("noteVersions");
        int versionCount = 0;
        if (versions != null) {
            for (Map<String, Object> v : versions) {
                NoteVersion version = importMapper.convertValue(v, NoteVersion.class);
                version.setId(null);
                noteVersionMapper.insert(version);
                versionCount++;
            }
        }
        result.put("noteVersions", versionCount);

        result.put("message", "数据恢复完成");
        return result;
    }

    @Override
    public Map<String, Object> getBackupInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("notes", noteMapper.selectCount(null));
        info.put("folders", folderMapper.selectCount(null));
        info.put("comments", commentMapper.selectCount(null));
        info.put("noteVersions", noteVersionMapper.selectCount(null));
        info.put("aiConversations", aiConversationMapper.selectCount(null));
        info.put("aiChatMessages", aiChatMessageMapper.selectCount(null));
        info.put("users", userMapper.selectCount(null));
        return info;
    }
}
