package com.nexus.stack.brain.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.datasource.clickhouse")
public class ClickHouseProperties {
    private String jdbcUrl;
    private String username;
    private String password;
    private String driverClassName;
}
