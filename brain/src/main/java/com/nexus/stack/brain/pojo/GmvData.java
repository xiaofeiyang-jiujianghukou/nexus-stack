package com.nexus.stack.brain.pojo;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GmvData implements Serializable {
    private LocalDateTime statTime;
    private BigDecimal gmv;

    // 用于 Redis 存储的字符串格式
    public String toRedisValue() {
        return String.format("%s:%.2f", statTime, gmv);
    }
}
