package com.nexus.stack.brain.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WideOrder implements java.io.Serializable {
    private Long orderId;
    private Long userId;
    private Double amount;
    private String level;
    //@JsonProperty("isMember") // 👈 强制 Jackson 使用这个名字
//    @Getter(onMethod_ = {@JsonProperty("isMember")}) // 👈 强制 Lombok 在 Getter 上加注解
//    @Setter(onMethod_ = {@JsonProperty("isMember")}) // 👈 强制 Lombok 在 Setter 上加注解
    @JsonProperty("isMember")
    private boolean isMember;
}
