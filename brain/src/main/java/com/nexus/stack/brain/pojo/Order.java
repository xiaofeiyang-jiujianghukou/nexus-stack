package com.nexus.stack.brain.pojo;

import lombok.*;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Order implements java.io.Serializable  {
    private Long orderId;
    private Long userId;
    private Double amount;
    private Long ts;
}
