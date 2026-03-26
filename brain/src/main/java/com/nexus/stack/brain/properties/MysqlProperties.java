package com.nexus.stack.brain.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.datasource.mysql")
public class MysqlProperties {
    private String jdbcUrl;
    private String username;
    private String password;
    private String driverClassName;
    private String hostname;
    private int port;
}
