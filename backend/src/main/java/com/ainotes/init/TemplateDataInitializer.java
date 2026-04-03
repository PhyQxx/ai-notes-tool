package com.ainotes.init;

import com.ainotes.entity.NoteTemplate;
import com.ainotes.mapper.NoteTemplateMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemplateDataInitializer implements CommandLineRunner {

    private final NoteTemplateMapper templateMapper;

    @Override
    public void run(String... args) {
        Long count = templateMapper.selectCount(
                new LambdaQueryWrapper<NoteTemplate>().eq(NoteTemplate::getCategory, "system"));
        if (count > 0) {
            log.info("系统模板已存在，跳过初始化");
            return;
        }

        log.info("初始化系统预置模板...");

        insertTemplate("会议纪要", "📋",
                "# {{标题}}\n\n## 会议信息\n- **日期**：{{日期}}\n- **时间**：{{时间}}\n- **地点**：\n- **主持人**：\n- **参会人员**：\n\n## 会议议程\n1. \n2. \n3. \n\n## 讨论内容\n\n### 议题一\n- 讨论要点：\n- 结论：\n\n### 议题二\n- 讨论要点：\n- 结论：\n\n## 待办事项\n| 任务 | 负责人 | 截止日期 | 状态 |\n|------|--------|----------|------|\n|      |        |          | 待开始 |\n\n## 下次会议\n- **时间**：\n- **议题**：\n",
                "[\"标题\",\"地点\",\"主持人\",\"参会人员\"]", 1);

        insertTemplate("周报", "📊",
                "# 周报 - {{日期}}\n\n## 本周完成\n\n### 项目进展\n- \n- \n\n### 日常工作\n- \n- \n\n## 遇到的问题\n1. \n\n## 解决方案\n1. \n\n## 下周计划\n1. \n2. \n3. \n\n## 需要协调的事项\n- \n",
                "[\"日期\"]", 2);

        insertTemplate("技术文档", "🔧",
                "# {{标题}}\n\n## 概述\n\n## 背景与目标\n\n## 技术方案\n\n### 架构设计\n\n### 核心流程\n\n### 数据模型\n\n## 接口设计\n\n### API 列表\n| 方法 | 路径 | 说明 |\n|------|------|------|\n| GET  |      |      |\n| POST |      |      |\n\n## 实现细节\n\n## 测试方案\n\n## 部署说明\n\n## 注意事项\n",
                "[\"标题\"]", 3);

        insertTemplate("读书笔记", "📚",
                "# 《{{标题}}》读书笔记\n\n## 基本信息\n- **作者**：\n- **出版时间**：\n- **阅读时间**：{{日期}}\n- **评分**：⭐⭐⭐⭐⭐\n\n## 内容摘要\n\n\n## 精彩摘录\n> \n> \n\n## 核心观点\n\n1. \n2. \n3. \n\n## 个人感悟\n\n\n## 行动清单\n- [ ] \n- [ ] \n\n## 推荐指数**：⭐⭐⭐⭐⭐\n",
                "[\"标题\",\"作者\"]", 4);

        insertTemplate("日记", "📝",
                "# {{日期}} {{星期}}\n\n## 今日心情\n😊 开心\n\n## 今日事项\n- \n- \n\n## 有趣的事\n\n\n## 感悟与反思\n\n\n## 明天计划\n- [ ] \n",
                "[\"日期\"]", 5);

        log.info("系统预置模板初始化完成，共 5 个模板");
    }

    private void insertTemplate(String name, String icon, String content, String variables, int sortOrder) {
        NoteTemplate t = new NoteTemplate();
        t.setName(name);
        t.setContent(content);
        t.setIcon(icon);
        t.setCategory("system");
        t.setVariables(variables);
        t.setSortOrder(sortOrder);
        templateMapper.insert(t);
    }
}
