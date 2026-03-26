package com.nexus.stack.brain.entity.clickhouse;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("dwd_order_wide")
public class OrderWideEntity {
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String level;
    private Integer isMember;
    private Long ts;
}
