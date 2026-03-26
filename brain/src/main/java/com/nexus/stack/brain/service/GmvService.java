package com.nexus.stack.brain.service;

import com.nexus.stack.brain.pojo.GmvData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GmvService {

    @Autowired
    @Qualifier("chJdbcTemplate")
    private JdbcTemplate clickhouseJdbcTemplate;        // ClickHouse

    public List<GmvData> getGmv(String sql) {
        return clickhouseJdbcTemplate.query(sql, (rs, rowNum) ->
                new GmvData(
                        rs.getTimestamp("stat_time").toLocalDateTime(),
                        rs.getBigDecimal("gmv")
                )
        );
    }

    // 例：过去24小时，每2小时1档
    public List<GmvData> getGmvByRangeAndInterval(String range, String interval) {
        String sql = String.format(
                "SELECT toStartOfInterval(stat_time, INTERVAL %s) AS stat_time, " +
                        "SUM(gmv) AS gmv " +
                        "FROM ads_gmv_1m " +
                        "WHERE stat_time >= now() - INTERVAL %s " +
                        "GROUP BY stat_time " +
                        "ORDER BY stat_time ASC",
                interval, range
        );
        return getGmv(sql);
    }

    // 其他时间段类似
}
