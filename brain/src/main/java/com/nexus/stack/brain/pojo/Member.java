package com.nexus.stack.brain.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Member implements java.io.Serializable  {
    private Long userId;
    //@JsonProperty("isMember") // 👈 强制 Jackson 使用这个名字
//    @Getter(onMethod_ = {@JsonProperty("isMember")}) // 👈 强制 Lombok 在 Getter 上加注解
//    @Setter(onMethod_ = {@JsonProperty("isMember")}) // 👈 强制 Lombok 在 Setter 上加注解
    private boolean isMember;
    private Long ts;
}
