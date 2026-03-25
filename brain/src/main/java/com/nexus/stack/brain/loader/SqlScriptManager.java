package com.nexus.stack.brain.loader;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StreamUtils;
import org.springframework.core.env.Environment;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class SqlScriptManager {

    private final ResourceLoader resourceLoader;
    private final Environment env;
    private final PropertyPlaceholderHelper placeholderHelper;

    public SqlScriptManager(ResourceLoader resourceLoader, Environment env) {
        this.resourceLoader = resourceLoader;
        this.env = env;
        this.placeholderHelper = new PropertyPlaceholderHelper("${", "}");
    }

    /**
     * 读取 SQL 文件，自动去掉注释，并替换 application.yml 中的变量
     */
    public String load(String path) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + path);
        if (!resource.exists()) {
            throw new IOException("SQL文件不存在: " + path);
        }

        // 1. 读取原始文本
        String rawSql = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

        // 2. 替换变量 (例如 ${kafka.bootstrap-servers})
        String resolvedSql = placeholderHelper.replacePlaceholders(rawSql, env::getProperty);

        // 3. 清洗：去掉单行注释，去掉多余换行
        return resolvedSql.replaceAll("(?m)^\\s*--.*$", " ")
                .replace("\n", " ")
                .replace("\r", " ")
                .trim();
    }
}
