package com.nexus.stack.brain.controller;

import com.alibaba.fastjson2.JSON;
import com.nexus.stack.brain.pojo.GmvAllResponse;
import com.nexus.stack.brain.pojo.GmvData;
import com.nexus.stack.brain.redis.RedisHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.core.type.TypeReference;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nexus.stack.brain.consts.RedisConsts.*;

@Slf4j
@RestController
@RequestMapping("/dashboard")
public class GmvDashboardController {

    @Autowired
    private RedisHelper redisHelper;

    /**
     * 一次性返回所有粒度 GMV
     */
    @GetMapping("/gmvAll")
    public GmvAllResponse gmvAll() {
        GmvAllResponse response = new GmvAllResponse();

        // 增加用户数/会员数
        response.setUserCount(redisHelper.getValue(USER_COUNT, Integer.class));
        response.setVipCount(redisHelper.getValue(VIP_COUNT, Integer.class));

        // 1️⃣ 单值 GMV（秒/分钟/小时/天/周/月/年）
        response.setSeconds(redisHelper.getValue(GMV_FOR_SECONDS, BigDecimal.class));
        response.setMinute(redisHelper.getValue(GMV_FOR_MINUTE, BigDecimal.class));
        response.setHalfHour(redisHelper.getValue(GMV_FOR_HALF_HOUR, BigDecimal.class));
        response.setHour(redisHelper.getValue(GMV_FOR_HOUR, BigDecimal.class));
        response.setHalfDay(redisHelper.getValue(GMV_FOR_HALF_DAY, BigDecimal.class));
        response.setDay(redisHelper.getValue(GMV_FOR_DAY, BigDecimal.class));
        response.setWeek(redisHelper.getValue(GMV_FOR_WEEK, BigDecimal.class));
        response.setHalfMonth(redisHelper.getValue(GMV_FOR_HALF_MONTH, BigDecimal.class));
        response.setMonth(redisHelper.getValue(GMV_FOR_MONTH, BigDecimal.class));
        response.setHalfYear(redisHelper.getValue(GMV_FOR_HALF_YEAR, BigDecimal.class));
        response.setYear(redisHelper.getValue(GMV_FOR_YEAR, BigDecimal.class));

        // 2️⃣ 各档位历史数据 overview
        Map<String, Object> overviewMap = redisHelper.getHashAll(GMV_OVERVIEW, Object.class);

        Map<String, List<GmvData>> overview = new HashMap<>();
        overviewMap.forEach((field, value) -> {
            // 先把 value 转成字符串
            String json = value.toString();
            // 单独调用 JSON.parseArray
            List<GmvData> list = JSON.parseArray(json, GmvData.class);
            overview.put(field, list);
        });

        response.setOverview(overview);

        return response;
    }
}