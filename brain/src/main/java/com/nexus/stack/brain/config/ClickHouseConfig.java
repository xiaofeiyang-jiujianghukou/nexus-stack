//package com.nexus.stack.brain.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.datasource.DriverManagerDataSource;
//
//@Configuration
//public class ClickHouseConfig {
//
//    @Bean(name = "clickHouseJdbcTemplate")
//    public JdbcTemplate clickHouseJdbcTemplate() {
//        // 手动构建简易数据源，适合单体快速学习
//        DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setDriverClassName("com.clickhouse.jdbc.ClickHouseDriver");
//        dataSource.setUrl("jdbc:ch://127.0.0.1:28123/default");
//        dataSource.setUsername("default");
//        dataSource.setPassword("");
//        return new JdbcTemplate(dataSource);
//    }
//}
