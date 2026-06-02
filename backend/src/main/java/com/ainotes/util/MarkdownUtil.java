package com.ainotes.util;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.EncryptionConstants;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import lombok.extern.slf4j.Slf4j;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Markdown工具类
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class MarkdownUtil {

    private static final Parser PARSER;
    private static final HtmlRenderer RENDERER;

    static {
        // 创建支持GFM表格的解析器
        List<Extension> extensions = Arrays.asList(TablesExtension.create());
        PARSER = Parser.builder()
                .extensions(extensions)
                .build();

        RENDERER = HtmlRenderer.builder()
                .extensions(extensions)
                .build();
    }

    /**
     * 将Markdown转换为HTML
     */
    public static String markdownToHtml(String title, String content, String author, String createdAt) {
        // 解析Markdown
        Node document = PARSER.parse(content);
        String bodyHtml = RENDERER.render(document);

        // 构建完整的HTML文档
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"zh-CN\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <title>").append(title).append("</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: 'Microsoft YaHei', sans-serif; line-height: 1.6; margin: 20px; }\n");
        html.append("        h1 { border-bottom: 2px solid #eee; }\n");
        html.append("        table { border-collapse: collapse; width: 100%; }\n");
        html.append("        th, td { border: 1px solid #ddd; padding: 8px; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <h1>").append(title).append("</h1>\n");
        html.append(bodyHtml);
        html.append("</body>\n");
        html.append("</html>");

        return html.toString();
    }

    /**
     * 将HTML转换为PDF（支持密码保护）
     */
    public static byte[] htmlToPdf(String html, String password) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        if (password != null && !password.isEmpty()) {
            WriterProperties props = new WriterProperties();
            props.setStandardEncryption(
                password.getBytes(StandardCharsets.UTF_8), 
                password.getBytes(StandardCharsets.UTF_8), 
                EncryptionConstants.ALLOW_PRINTING, 
                EncryptionConstants.ENCRYPTION_AES_128
            );
            PdfWriter writer = new PdfWriter(outputStream, props);
            HtmlConverter.convertToPdf(html, writer);
        } else {
            HtmlConverter.convertToPdf(html, outputStream);
        }
        
        return outputStream.toByteArray();
    }

    public static Parser getParser() { return PARSER; }
    public static HtmlRenderer getRenderer() { return RENDERER; }
}
