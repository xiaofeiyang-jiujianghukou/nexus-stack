package com.nexus.stack.brain.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class MultiJdbcConfig {

    @Bean(name = "mysqlDataSource")
    @Primary // 将 MySQL 设为主数据源
    @ConfigurationProperties(prefix = "app.datasource.mysql")
    public DataSource mysqlDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "chDataSource")
    @ConfigurationProperties(prefix = "app.datasource.clickhouse")
    public DataSource clickhouseDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    public JdbcTemplate mysqlJdbcTemplate(DataSource mysqlDataSource) {
        log.info("mysqlDataSource Url -> {}", ((com.zaxxer.hikari.HikariDataSource) mysqlDataSource).getJdbcUrl());
        return new JdbcTemplate(mysqlDataSource);
    }

    @Bean(name = "chJdbcTemplate")
    public JdbcTemplate clickhouseJdbcTemplate(
            @Qualifier("chDataSource") DataSource clickhouseDataSource) {
        log.info("clickhouseDataSource Url -> {}", ((com.zaxxer.hikari.HikariDataSource) clickhouseDataSource).getJdbcUrl());
        return new JdbcTemplate(clickhouseDataSource);
    }
}
