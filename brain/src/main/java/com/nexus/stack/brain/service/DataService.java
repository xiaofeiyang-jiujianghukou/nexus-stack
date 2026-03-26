package com.nexus.stack.brain.service;

import com.nexus.stack.brain.entity.clickhouse.GmvEntity;
import com.nexus.stack.brain.entity.mysql.UserEntity;
import com.nexus.stack.brain.mapper.mysql.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class DataService {

    @Autowired
    private UserMapper userMapper;      // MySQL
    @Autowired
    @Qualifier("chJdbcTemplate")
    private JdbcTemplate clickhouseJdbcTemplate;        // ClickHouse

    public void queryData() {
        // 查询 MySQL
        List<UserEntity> users = userMapper.selectList(null);

        // 查询 ClickHouse
        GmvEntity latestGmv = clickhouseJdbcTemplate.queryForObject(
                "SELECT * FROM ads_gmv_1m ORDER BY stat_time DESC LIMIT 1",
                GmvEntity.class);

        BigDecimal gmv = clickhouseJdbcTemplate.queryForObject(
                "SELECT gmv FROM ads_gmv_1m ORDER BY stat_time DESC LIMIT 1",
                BigDecimal.class);

        log.info("用户数: {}, 最新GMV: {}", users.size(), latestGmv.getGmv());
    }

    public GmvEntity latestGmv() {
        return clickhouseJdbcTemplate.queryForObject(
                "SELECT * FROM ads_gmv_1m ORDER BY stat_time DESC LIMIT 1",
                (rs, rowNum) -> {
                    GmvEntity entity = new GmvEntity();
                    entity.setStatTime(rs.getTimestamp("stat_time").toLocalDateTime());
                    entity.setGmv(rs.getBigDecimal("gmv"));
                    return entity;
                });
    }

    public Integer orderTotalCount() {
        return clickhouseJdbcTemplate.queryForObject(
                "SELECT count() FROM dwd_order_wide",
                Integer.class);
    }
}