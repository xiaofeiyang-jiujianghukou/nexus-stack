package com.nexus.stack.brain.schedule;

import com.nexus.stack.brain.service.DataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderWideSyncJob {

    @Autowired
    private DataService dataService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Scheduled(fixedRate = 5000)
    public void fetchPushOrder() {
        Integer orderTotalCount = dataService.orderTotalCount();
        stringRedisTemplate.opsForValue().set("order_total_count", orderTotalCount.toString());

        log.debug("📊 查询到 GMV 数据: 订单总数={}", orderTotalCount.toString());
    }
}
