package com.ainotes.util;

import com.itextpdf.html2pdf.HtmlConverter;
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
     *
     * @param title     标题
     * @param content   内容
     * @param author    作者
     * @param createdAt 创建时间
     * @return HTML字符串
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
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>").append(title).append("</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: 'Microsoft YaHei', 'SimSun', sans-serif; line-height: 1.6; margin: 20px; }\n");
        html.append("        h1 { color: #333; border-bottom: 2px solid #eee; padding-bottom: 10px; }\n");
        html.append("        h2 { color: #444; margin-top: 30px; }\n");
        html.append("        h3 { color: #555; }\n");
        html.append("        p { margin: 10px 0; }\n");
        html.append("        code { background-color: #f4f4f4; padding: 2px 6px; border-radius: 3px; font-family: 'Courier New', monospace; }\n");
        html.append("        pre { background-color: #f4f4f4; padding: 15px; border-radius: 5px; overflow-x: auto; }\n");
        html.append("        pre code { background-color: transparent; padding: 0; }\n");
        html.append("        blockquote { border-left: 4px solid #ddd; margin: 0; padding-left: 20px; color: #666; }\n");
        html.append("        ul, ol { margin: 10px 0; padding-left: 30px; }\n");
        html.append("        li { margin: 5px 0; }\n");
        html.append("        table { border-collapse: collapse; width: 100%; margin: 20px 0; }\n");
        html.append("        th, td { border: 1px solid #ddd; padding: 8px 12px; text-align: left; }\n");
        html.append("        th { background-color: #f5f5f5; }\n");
        html.append("        tr:nth-child(even) { background-color: #f9f9f9; }\n");
        html.append("        a { color: #0066cc; text-decoration: none; }\n");
        html.append("        a:hover { text-decoration: underline; }\n");
        html.append("        .meta { color: #666; font-size: 12px; margin-bottom: 20px; }\n");
        html.append("        .divider { border-top: 1px solid #ddd; margin: 20px 0; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <h1>").append(title).append("</h1>\n");
        html.append("    <div class=\"meta\">\n");
        html.append("        <p><strong>作者：</strong>").append(author).append("</p>\n");
        html.append("        <p><strong>创建时间：</strong>").append(createdAt).append("</p>\n");
        html.append("    </div>\n");
        html.append("    <div class=\"divider\"></div>\n");
        html.append(bodyHtml);
        html.append("</body>\n");
        html.append("</html>");

        return html.toString();
    }

    /**
     * 将HTML转换为PDF
     *
     * @param html HTML字符串
     * @return PDF字节数组
     * @throws IOException IO异常
     */
    public static byte[] htmlToPdf(String html) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(html.getBytes(StandardCharsets.UTF_8), outputStream);
        return outputStream.toByteArray();
    }

    /**
     * 获取Markdown解析器
     *
     * @return 解析器
     */
    public static Parser getParser() {
        return PARSER;
    }

    /**
     * 获取HTML渲染器
     *
     * @return 渲染器
     */
    public static HtmlRenderer getRenderer() {
        return RENDERER;
    }

}
