package com.ainotes.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class FtpUtil {

    @Value("${ftp.host:127.0.0.1}")
    private String host;

    @Value("${ftp.port:21}")
    private int port;

    @Value("${ftp.username:anonymous}")
    private String username;

    @Value("${ftp.password:}")
    private String password;

    @Value("${ftp.base-path:/}")
    private String basePath;

    @Value("${ftp.url-prefix:http://127.0.0.1}")
    private String urlPrefix;

    private FTPClient connect() {
        try {
            FTPClient client = new FTPClient();
            client.setControlEncoding("UTF-8");
            log.info("FTP连接: host={}, port={}, user={}", host, port, username);
            client.connect(host, port);
            int connectReply = client.getReplyCode();
            log.info("FTP连接响应: code={}", connectReply);
            if (!FTPReply.isPositiveCompletion(connectReply)) {
                log.error("FTP连接被拒绝, replyCode={}", connectReply);
                client.disconnect();
                return null;
            }
            if (!client.login(username, password)) {
                log.error("FTP登录失败, user={}", username);
                client.disconnect();
                return null;
            }
            client.enterLocalPassiveMode();
            log.info("FTP登录成功, replyCode={}", client.getReplyCode());
            return client;
        } catch (IOException e) {
            log.error("FTP连接异常: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    /**
     * 上传文件到FTP
     *
     * @param data     文件字节数据
     * @param dir      目录名（如 image、attachment）
     * @param fileName 文件名
     * @return 文件访问URL，失败返回null
     */
    public String upload(byte[] data, String dir, String fileName) {
        FTPClient client = connect();
        if (client == null) {
            log.error("FTP upload: 连接失败，返回null");
            return null;
        }
        try {
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String base = basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath;
            String fullDir = base + "/" + dir + "/" + dateDir;
            log.info("FTP upload: dir={}, fileName={}, dataLen={}", fullDir, fileName, data.length);
            makeDirs(client, fullDir);
            client.changeWorkingDirectory(fullDir);
            client.setFileType(FTP.BINARY_FILE_TYPE);
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            boolean ok = client.storeFile(fileName, in);
            in.close();
            log.info("FTP storeFile result: {}, replyCode={}", ok, client.getReplyCode());
            if (ok) {
                String prefix = urlPrefix.endsWith("/") ? urlPrefix.substring(0, urlPrefix.length() - 1) : urlPrefix;
                String url = prefix + "/" + dir + "/" + dateDir + "/" + fileName;
                log.info("FTP upload 成功: {}", url);
                return url;
            }
            log.error("FTP storeFile失败, replyCode={}, reply={}", client.getReplyCode(), client.getReplyString());
            return null;
        } catch (IOException e) {
            log.error("FTP上传异常: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return null;
        } finally {
            disconnect(client);
        }
    }

    /**
     * 从FTP下载文件
     *
     * @param filePath 文件相对路径（如 image/2026/06/02/uuid.jpg）
     * @return 文件字节数据，失败返回null
     */
    public byte[] download(String filePath) {
        FTPClient client = connect();
        if (client == null) return null;
        try {
            client.setFileType(FTP.BINARY_FILE_TYPE);
            String base = basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath;
            String fullPath = base + "/" + filePath;
            try (InputStream is = client.retrieveFileStream(fullPath)) {
                if (is == null) return null;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int len;
                while ((len = is.read(buf)) != -1) {
                    bos.write(buf, 0, len);
                }
                client.completePendingCommand();
                return bos.toByteArray();
            }
        } catch (IOException e) {
            log.error("FTP下载失败: {}", e.getMessage());
            return null;
        } finally {
            disconnect(client);
        }
    }

    /**
     * 从FTP删除文件
     *
     * @param filePath 文件相对路径（如 image/2026/06/02/uuid.jpg）
     * @return 是否删除成功
     */
    public boolean delete(String filePath) {
        FTPClient client = connect();
        if (client == null) return false;
        try {
            String base = basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath;
            String fullPath = base + "/" + filePath;
            return client.deleteFile(fullPath);
        } catch (IOException e) {
            log.error("FTP删除失败: {}", e.getMessage());
            return false;
        } finally {
            disconnect(client);
        }
    }

    private void makeDirs(FTPClient client, String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String d : path.split("/")) {
            if (d.isEmpty()) continue;
            sb.append("/").append(d);
            try {
                if (!client.changeWorkingDirectory(sb.toString())) {
                    client.makeDirectory(sb.toString());
                    client.changeWorkingDirectory(sb.toString());
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void disconnect(FTPClient client) {
        try {
            if (client != null && client.isConnected()) {
                client.logout();
                client.disconnect();
            }
        } catch (IOException ignored) {
        }
    }
}
