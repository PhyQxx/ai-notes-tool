package com.ainotes.task;

import com.ainotes.config.KnowledgeAssistant;
import com.ainotes.entity.Note;
import com.ainotes.entity.User;
import com.ainotes.mapper.UserMapper;
import com.ainotes.service.NoteService;
import com.ainotes.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 知识洞察任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InsightTask {

    private final UserMapper userMapper;
    private final NoteService noteService;
    private final KnowledgeAssistant knowledgeAssistant;
    private final NotificationService notificationService;

    /**
     * 每周一早上 9 点生成上周知识周报
     */
    @Scheduled(cron = "0 0 9 ? * MON")
    public void generateWeeklyInsight() {
        log.info("开始生成 AI 知识周报...");
        List<User> users = userMapper.selectList(null);
        
        for (User user : users) {
            try {
                // 1. 获取用户上周更新的笔记
                LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
                List<Note> recentNotes = noteService.recentNotes(user.getId(), 20).stream()
                        .filter(n -> n.getUpdatedAt().isAfter(lastWeek))
                        .collect(Collectors.toList());

                if (recentNotes.isEmpty()) continue;

                // 2. 构建提示词
                String titles = recentNotes.stream().map(Note::getTitle).collect(Collectors.joining(", "));
                String prompt = String.format(
                    "以下是我上周记录或更新的笔记标题：[%s]。请为我生成一份简短的知识周报，总结我的学习/工作重点，并提供2-3条下一步的建议。请使用鼓励性的语气，保持简洁（200字以内）。",
                    titles
                );

                // 3. 调用 AI 生成
                String insight = knowledgeAssistant.answer(prompt);

                // 4. 发送系统通知
                notificationService.sendNotification(
                    user.getId(),
                    "AI 知识周报",
                    insight,
                    "insight",
                    null
                );
                
                log.info("用户 {} 的周报生成成功", user.getId());
            } catch (Exception e) {
                log.error("用户 {} 周报生成失败: {}", user.getId(), e.getMessage());
            }
        }
    }
}
