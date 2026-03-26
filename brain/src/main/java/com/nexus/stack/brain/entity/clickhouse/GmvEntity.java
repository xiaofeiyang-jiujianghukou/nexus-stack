package com.nexus.stack.brain.entity.clickhouse;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ads_gmv_1m")
public class GmvEntity {
    private LocalDateTime statTime;
    private BigDecimal gmv;
}
