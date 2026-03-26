package com.nexus.stack.brain.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

import static com.nexus.stack.brain.consts.RedisConsts.*;

@Slf4j
@RestController
@RequestMapping("/dashboard")
public class OrderDashboardController {

    private final JdbcTemplate mysqlJdbcTemplate;
    private final JdbcTemplate chJdbcTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public OrderDashboardController(JdbcTemplate mysqlJdbcTemplate, @Qualifier("chJdbcTemplate") JdbcTemplate chJdbcTemplate) {
        this.mysqlJdbcTemplate = mysqlJdbcTemplate;
        this.chJdbcTemplate = chJdbcTemplate;
    }

    @GetMapping("/gmvForSecond")
    public BigDecimal gmvForSecond() {
        String value = redisTemplate.opsForValue().get(GMV_FOR_SECONDS);
        return value != null ? new BigDecimal(value) : new BigDecimal("0.0");
    }

    @GetMapping("/gmvForMinute")
    public BigDecimal gmvForMinute() {
        String value = redisTemplate.opsForValue().get(GMV_FOR_MINUTE);
        return value != null ? new BigDecimal(value) : new BigDecimal("0.0");
    }

    @GetMapping("/gmvForHour")
    public BigDecimal gmvForHour() {
        String value = redisTemplate.opsForValue().get(GMV_FOR_HOUR);
        return value != null ? new BigDecimal(value) : new BigDecimal("0.0");
    }

    @GetMapping("/gmvForDay")
    public BigDecimal gmvForDay() {
        String value = redisTemplate.opsForValue().get(GMV_FOR_DAY);
        return value != null ? new BigDecimal(value) : new BigDecimal("0.0");
    }

    @GetMapping("/gmvForWeek")
    public BigDecimal gmvForSevenDay() {
        String value = redisTemplate.opsForValue().get(GMV_FOR_WEEK);
        return value != null ? new BigDecimal(value) : new BigDecimal("0.0");
    }

    @GetMapping("/gmvForMonth")
    public BigDecimal gmvForMonth() {
        String value = redisTemplate.opsForValue().get(GMV_FOR_MONTH);
        return value != null ? new BigDecimal(value) : new BigDecimal("0.0");
    }

    @GetMapping("/gmvForYear")
    public BigDecimal gmvForYear() {
        String value = redisTemplate.opsForValue().get(GMV_FOR_YEAR);
        return value != null ? new BigDecimal(value) : new BigDecimal("0.0");
    }

    @GetMapping("/fetchOrderCount")
    public Integer fetchOrderCount() {
        String value = redisTemplate.opsForValue().get("order_total_count");
        return value != null ? Integer.parseInt(value) : 0;
    }

    /*@GetMapping("/gmv")
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
    }*/


}
