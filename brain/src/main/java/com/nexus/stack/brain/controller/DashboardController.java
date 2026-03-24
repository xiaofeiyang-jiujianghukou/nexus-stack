package com.nexus.stack.brain.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final JdbcTemplate mysqlJdbcTemplate;
    private final JdbcTemplate chJdbcTemplate;

    public DashboardController(JdbcTemplate mysqlJdbcTemplate, @Qualifier("chJdbcTemplate") JdbcTemplate chJdbcTemplate) {
        this.mysqlJdbcTemplate = mysqlJdbcTemplate;
        this.chJdbcTemplate = chJdbcTemplate;
    }

    @GetMapping("/gmv")
    public List<Map<String, Object>> gmv() {
        return chJdbcTemplate.queryForList(
                "SELECT * FROM ads_gmv_1m ORDER BY window_start DESC LIMIT 10"
        );
    }

    @GetMapping("/checkMetaData")
    public Map<String, Object> checkMetaData() throws SQLException {
        Map<String, Object> map = new HashMap<>();
        if (mysqlJdbcTemplate.getDataSource() != null) {
            String url = mysqlJdbcTemplate.getDataSource().getConnection().getMetaData().getURL();
            map.put("MySQL URL", url);
        }
        if (chJdbcTemplate.getDataSource() != null) {
            String url = chJdbcTemplate.getDataSource().getConnection().getMetaData().getURL();
            map.put("CH URL", url);
        }
        log.info("MySQL URL: " + mysqlJdbcTemplate.getDataSource().getConnection().getMetaData().getURL());
        log.info("CH URL: " + chJdbcTemplate.getDataSource().getConnection().getMetaData().getURL());
        return map;
    }


}
