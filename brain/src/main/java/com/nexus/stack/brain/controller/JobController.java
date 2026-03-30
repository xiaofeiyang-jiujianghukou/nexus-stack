package com.nexus.stack.brain.controller;

import com.nexus.stack.brain.job.cdc.rest.GmvSqlSubmitter;
import com.nexus.stack.brain.job.cdc.rest.OrderWideSqlSubmitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/job")
public class JobController {

    @Value("${flink.rest.url}")
    private String flinkRestUrl;

    @Value("${flink.sql-gateway.url}")
    private String sqlGatewayUrl;

    /**
     * 执行GMV任务
     */
    @GetMapping("/gmv")
    public String gmv() {
        GmvSqlSubmitter.run(sqlGatewayUrl);

        return "gmv 执行成功";
    }

    /**
     * 执行orderWide任务
     */
    @GetMapping("/orderWide")
    public String orderWide() {
        OrderWideSqlSubmitter.run(sqlGatewayUrl);

        return "orderWide 执行成功";
    }
}