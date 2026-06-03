package com.ainotes.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ftp")
public class FtpConfig {

    private String host = "127.0.0.1";
    private int port = 21;
    private String username = "anonymous";
    private String password = "";
    private String basePath = "/";
    private String urlPrefix = "http://127.0.0.1";
}
