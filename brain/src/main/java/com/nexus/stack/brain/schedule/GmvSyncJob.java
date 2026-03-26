package com.nexus.stack.brain.schedule;

import com.alibaba.fastjson2.JSON;
import com.nexus.stack.brain.entity.clickhouse.GmvEntity;
import com.nexus.stack.brain.pojo.GmvData;
import com.nexus.stack.brain.service.DataService;
import com.nexus.stack.brain.service.GmvService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.nexus.stack.brain.consts.RedisConsts.*;

@Component
@Slf4j
public class GmvSyncJob {

    @Autowired
    private DataService dataService;
    @Autowired
    private GmvService gmvService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    @Qualifier("chJdbcTemplate")
    private JdbcTemplate clickhouseJdbcTemplate;        // ClickHouse

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void gmvForSecond() {

        BigDecimal gmv = clickhouseJdbcTemplate.queryForObject(
                "SELECT gmv FROM ads_gmv_1m ORDER BY stat_time DESC LIMIT 1",
                BigDecimal.class);
        stringRedisTemplate.opsForValue().set(GMV_FOR_SECONDS, gmv.toString());

        // 1️⃣ 过去 1 分钟
        BigDecimal gmvMinute = clickhouseJdbcTemplate.queryForObject(
                "SELECT ROUND(SUM(gmv),2) FROM ads_gmv_1m WHERE stat_time >= now() - INTERVAL 1 MINUTE",
                BigDecimal.class);
        stringRedisTemplate.opsForValue().set(GMV_FOR_MINUTE, gmvMinute.toString());

        // 2️⃣ 过去 半 小时
        BigDecimal gmvHalfHour = clickhouseJdbcTemplate.queryForObject(
                "SELECT ROUND(SUM(gmv),2) FROM ads_gmv_1m WHERE stat_time >= now() - INTERVAL 30 MINUTE",
                BigDecimal.class);
        stringRedisTemplate.opsForValue().set(GMV_FOR_HALF_HOUR, gmvHalfHour.toString());

        // 2️⃣ 过去 1 小时
        BigDecimal gmvHour = clickhouseJdbcTemplate.queryForObject(
                "SELECT ROUND(SUM(gmv),2) FROM ads_gmv_1m WHERE stat_time >= now() - INTERVAL 1 HOUR",
                BigDecimal.class);
        stringRedisTemplate.opsForValue().set(GMV_FOR_HOUR, gmvHour.toString());

        // 3️⃣ 过去 半 天
        BigDecimal gmvHalfDay = clickhouseJdbcTemplate.queryForObject(
                "SELECT ROUND(SUM(gmv),2) FROM ads_gmv_1m WHERE stat_time >= now() - INTERVAL 12 HOUR",
                BigDecimal.class);
        stringRedisTemplate.opsForValue().set(GMV_FOR_HALF_DAY, gmvHalfDay.toString());

        // 3️⃣ 过去 1 天
        BigDecimal gmvDay = clickhouseJdbcTemplate.queryForObject(
                "SELECT ROUND(SUM(gmv),2) FROM ads_gmv_1m WHERE stat_time >= now() - INTERVAL 1 DAY",
                BigDecimal.class);
        stringRedisTemplate.opsForValue().set(GMV_FOR_DAY, gmvDay.toString());

        // 3️⃣ 过去 1 周
        BigDecimal gmvWeek = clickhouseJdbcTemplate.queryForObject(
                "SELECT ROUND(SUM(gmv),2) FROM ads_gmv_1m WHERE stat_time >= now() - INTERVAL 1 DAY",
                BigDecimal.class);
        stringRedisTemplate.opsForValue().set(GMV_FOR_WEEK, gmvWeek.toString());

        // 4️⃣ 过去 半 月
        BigDecimal gmvHalfMonth = clickhouseJdbcTemplate.queryForObject(
                "SELECT ROUND(SUM(gmv),2) FROM ads_gmv_1m WHERE stat_time >= now() - INTERVAL 15 DAY",
                BigDecimal.class);
        stringRedisTemplate.opsForValue().set(GMV_FOR_HALF_MONTH, gmvHalfMonth.toString());

        // 4️⃣ 过去 1 月
        BigDecimal gmvMonth = clickhouseJdbcTemplate.queryForObject(
                "SELECT ROUND(SUM(gmv),2) FROM ads_gmv_1m WHERE stat_time >= now() - INTERVAL 1 MONTH",
                BigDecimal.class);
        stringRedisTemplate.opsForValue().set(GMV_FOR_MONTH, gmvMonth.toString());

        // 5️⃣ 过去 半 年
        BigDecimal gmvHalfYear = clickhouseJdbcTemplate.queryForObject(
                "SELECT ROUND(SUM(gmv),2) FROM ads_gmv_1m WHERE stat_time >= now() - INTERVAL 6 MONTH",
                BigDecimal.class);
        stringRedisTemplate.opsForValue().set(GMV_FOR_HALF_YEAR, gmvHalfYear.toString());

        // 5️⃣ 过去 1 年
        BigDecimal gmvYear = clickhouseJdbcTemplate.queryForObject(
                "SELECT ROUND(SUM(gmv),2) FROM ads_gmv_1m WHERE stat_time >= now() - INTERVAL 1 YEAR",
                BigDecimal.class);
        stringRedisTemplate.opsForValue().set(GMV_FOR_YEAR, gmvYear.toString());

        log.info("✅ GMV 更新完成: 10= {}, 1m={}, 1h={}, 1d={}, 1M={}, 1Y={}",
                gmv, gmvMinute, gmvHour, gmvDay, gmvMonth, gmvYear);

    }

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void updateGmvOverview() {

        List<GmvData> gmvByMinute = gmvService.getGmvByRangeAndInterval("1 MINUTE", "1 SECOND");// 过去1分钟，10秒钟1档
        stringRedisTemplate.opsForHash().put(GMV_OVERVIEW, GMV_FOR_MINUTE, JSON.toJSONString(gmvByMinute));

        List<GmvData> gmvByHalfHour = gmvService.getGmvByRangeAndInterval("30 MINUTE", "3 MINUTE");// 过去30分钟，3分钟1档
        stringRedisTemplate.opsForHash().put(GMV_OVERVIEW, GMV_FOR_HALF_HOUR, JSON.toJSONString(gmvByHalfHour));

        List<GmvData> gmvByHour = gmvService.getGmvByRangeAndInterval("1 HOUR", "5 MINUTE");// 过去1小时，5分钟1档
        stringRedisTemplate.opsForHash().put(GMV_OVERVIEW, GMV_FOR_HOUR, JSON.toJSONString(gmvByHour));

        List<GmvData> gmvByHalfDay = gmvService.getGmvByRangeAndInterval("12 HOUR", "1 HOUR");// 过去12小时，1小时1档
        stringRedisTemplate.opsForHash().put(GMV_OVERVIEW, GMV_FOR_HALF_DAY, JSON.toJSONString(gmvByHalfDay));

        List<GmvData> gmvByDay = gmvService.getGmvByRangeAndInterval("24 HOUR", "2 HOUR");// 过去24小时，2小时1档
        stringRedisTemplate.opsForHash().put(GMV_OVERVIEW, GMV_FOR_DAY, JSON.toJSONString(gmvByDay));

        List<GmvData> gmvByWeek = gmvService.getGmvByRangeAndInterval("1 WEEK", "12 HOUR");// 过去1周，12小时1档
        stringRedisTemplate.opsForHash().put(GMV_OVERVIEW, GMV_FOR_WEEK, JSON.toJSONString(gmvByWeek));

        List<GmvData> gmvByHalfMonth = gmvService.getGmvByRangeAndInterval("15 DAY", "1 DAY");   // 过去半月，每天1档
        stringRedisTemplate.opsForHash().put(GMV_OVERVIEW, GMV_FOR_HALF_MONTH, JSON.toJSONString(gmvByHalfMonth));

        List<GmvData> gmvByMonth = gmvService.getGmvByRangeAndInterval("1 MONTH", "2 DAY");  // 过去1月，每2天1档
        stringRedisTemplate.opsForHash().put(GMV_OVERVIEW, GMV_FOR_MONTH, JSON.toJSONString(gmvByMonth));

        List<GmvData> gmvByHalfYear = gmvService.getGmvByRangeAndInterval("6 MONTH", "15 DAY"); // 过去半年，每半月1档
        stringRedisTemplate.opsForHash().put(GMV_OVERVIEW, GMV_FOR_HALF_YEAR, JSON.toJSONString(gmvByHalfYear));

        List<GmvData> gmvByYear = gmvService.getGmvByRangeAndInterval("1 YEAR", "1 MONTH"); // 过去1年，每月1档
        stringRedisTemplate.opsForHash().put(GMV_OVERVIEW, GMV_FOR_YEAR, JSON.toJSONString(gmvByYear));

        log.info("✅ GMV 更新视图完成");
    }
}
