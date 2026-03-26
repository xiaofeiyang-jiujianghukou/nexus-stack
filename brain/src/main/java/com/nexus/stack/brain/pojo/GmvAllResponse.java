package com.nexus.stack.brain.pojo;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GmvAllResponse {
    private BigDecimal seconds;
    private BigDecimal minute;
    private BigDecimal halfHour;
    private BigDecimal hour;
    private BigDecimal halfDay;
    private BigDecimal day;
    private BigDecimal week;
    private BigDecimal halfMonth;
    private BigDecimal month;
    private BigDecimal halfYear;
    private BigDecimal year;
    private Map<String, List<GmvData>> overview;
}
