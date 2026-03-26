package com.nexus.stack.brain.mapper.mysql;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexus.stack.brain.entity.mysql.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

    @Select("SELECT * FROM users WHERE level = 'VIP'")
    List<UserEntity> selectVipUsers();
}
