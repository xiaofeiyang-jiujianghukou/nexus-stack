package com.nexus.stack.brain.pojo;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User implements java.io.Serializable  {
    private Long userId;
    private String level;
    private Long registerTime;
    private Long ts;
}
