package com.ainotes.service.impl;

import com.ainotes.common.exception.BusinessException;
import com.ainotes.entity.Note;
import com.ainotes.entity.User;
import com.ainotes.mapper.NoteMapper;
import com.ainotes.mapper.UserMapper;
import com.ainotes.service.ExportService;
import com.ainotes.util.MarkdownUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * 导出服务实现类
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private final NoteMapper noteMapper;
    private final UserMapper userMapper;

    @Override
    public ResponseEntity<byte[]> exportMarkdown(Long userId, Long noteId) {
        // 查询笔记
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }

        // 校验权限
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权限导出该笔记");
        }

        // 构建Markdown内容
        StringBuilder content = new StringBuilder();
        content.append("# ").append(note.getTitle()).append("\n\n");
        content.append("**作者：** ").append(getNickname(note.getUserId())).append("\n\n");
        content.append("**创建时间：** ")
                .append(note.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("\n\n");
        content.append("---\n\n");
        content.append(note.getContent());

        // 转换为字节数组
        byte[] bytes = content.toString().getBytes(StandardCharsets.UTF_8);

        // 构建文件名
        String filename = sanitizeFilename(note.getTitle()) + ".md";

        // 构建响应
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/markdown; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", URLEncoder.encode(filename, StandardCharsets.UTF_8));
        headers.setContentLength(bytes.length);

        log.info("导出Markdown文件成功，笔记ID：{}", noteId);

        return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
    }

    @Override
    public ResponseEntity<byte[]> exportPDF(Long userId, Long noteId) {
        // 查询笔记
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }

        // 校验权限
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权限导出该笔记");
        }

        try {
            // 将Markdown转换为HTML
            String html = MarkdownUtil.markdownToHtml(note.getTitle(), note.getContent(),
                    getNickname(note.getUserId()),
                    note.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // 使用iText将HTML转换为PDF
            byte[] pdfBytes = MarkdownUtil.htmlToPdf(html);

            // 构建文件名
            String filename = sanitizeFilename(note.getTitle()) + ".pdf";

            // 构建响应
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", URLEncoder.encode(filename, StandardCharsets.UTF_8));
            headers.setContentLength(pdfBytes.length);

            log.info("导出PDF文件成功，笔记ID：{}", noteId);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("导出PDF文件失败，笔记ID：{}", noteId, e);
            throw new BusinessException("导出PDF文件失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<byte[]> exportWord(Long userId, Long noteId) {
        // 查询笔记
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }

        // 校验权限
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权限导出该笔记");
        }

        try {
            // 使用POI创建Word文档
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            XWPFDocument document = new XWPFDocument();

            // 添加标题
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText(note.getTitle());
            titleRun.setBold(true);
            titleRun.setFontSize(18);
            titleRun.setFontFamily("宋体");

            // 添加元数据
            XWPFParagraph metaParagraph = document.createParagraph();
            metaParagraph.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun metaRun = metaParagraph.createRun();
            metaRun.setText("作者：" + getNickname(note.getUserId()));
            metaRun.setFontSize(11);
            metaRun.setFontFamily("宋体");

            XWPFParagraph timeParagraph = document.createParagraph();
            timeParagraph.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun timeRun = timeParagraph.createRun();
            timeRun.setText("创建时间：" +
                    note.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            timeRun.setFontSize(11);
            timeRun.setFontFamily("宋体");

            // 添加分隔线
            XWPFParagraph lineParagraph = document.createParagraph();
            lineParagraph.setSpacingAfter(200);
            XWPFRun lineRun = lineParagraph.createRun();
            lineRun.setText("--------------------------------------------------");

            // 添加内容
            // 简单处理：将Markdown内容直接作为文本添加
            XWPFParagraph contentParagraph = document.createParagraph();
            contentParagraph.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun contentRun = contentParagraph.createRun();
            contentRun.setText(note.getContent());
            contentRun.setFontSize(12);
            contentRun.setFontFamily("宋体");

            // 写入输出流
            document.write(outputStream);
            document.close();

            byte[] bytes = outputStream.toByteArray();

            // 构建文件名
            String filename = sanitizeFilename(note.getTitle()) + ".docx";

            // 构建响应
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
            headers.setContentDispositionFormData("attachment", URLEncoder.encode(filename, StandardCharsets.UTF_8));
            headers.setContentLength(bytes.length);

            log.info("导出Word文件成功，笔记ID：{}", noteId);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(bytes);

        } catch (IOException e) {
            log.error("导出Word文件失败，笔记ID：{}", noteId, e);
            throw new BusinessException("导出Word文件失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户昵称
     *
     * @param userId 用户ID
     * @return 昵称
     */
    private String getNickname(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null && user.getNickname() != null) {
            return user.getNickname();
        }
        return "未知用户";
    }

    /**
     * 清理文件名，移除非法字符
     *
     * @param filename 原始文件名
     * @return 清理后的文件名
     */
    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "note";
        }
        return filename.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

}
