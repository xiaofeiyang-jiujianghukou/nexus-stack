package com.nexus.stack.brain.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DataService {

    private final JdbcTemplate mysqlJdbcTemplate;
    private final JdbcTemplate chJdbcTemplate;

    public DataService(JdbcTemplate mysqlJdbcTemplate, @Qualifier("chJdbcTemplate") JdbcTemplate chJdbcTemplate) {
        this.mysqlJdbcTemplate = mysqlJdbcTemplate;
        this.chJdbcTemplate = chJdbcTemplate;
    }

    public void processOrder(String orderNo) {
        // 1. 存入 MySQL (事务性)
        mysqlJdbcTemplate.update("INSERT INTO t_order...", orderNo);
        // 2. 存入 ClickHouse (分析用)
        chJdbcTemplate.update("INSERT INTO brain_order_flow...", orderNo);
    }
}
