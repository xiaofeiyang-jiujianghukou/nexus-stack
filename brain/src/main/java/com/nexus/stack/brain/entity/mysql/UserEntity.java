package com.nexus.stack.brain.entity.mysql;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class UserEntity {
    private Long userId;
    private String level;
    private Long registerTime;
}
