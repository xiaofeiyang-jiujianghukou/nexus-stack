package com.nexus.stack.brain.redis;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class RedisHelper {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // ===================== opsForValue =====================
    /**
     * 泛型方法，从 Redis 获取单值 Key
     */
    public <T> T getValue(String key, Class<T> clazz) {
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            if (clazz == BigDecimal.class) return clazz.cast(BigDecimal.ZERO);
            if (clazz == Integer.class) return clazz.cast(0);
            if (clazz == Long.class) return clazz.cast(0L);
            if (clazz == Double.class) return clazz.cast(0.0);
            return null;
        }

        if (clazz == String.class) return clazz.cast(value);
        if (clazz == BigDecimal.class) return clazz.cast(new BigDecimal(value));
        if (clazz == Integer.class) return clazz.cast(Integer.valueOf(value));
        if (clazz == Long.class) return clazz.cast(Long.valueOf(value));
        if (clazz == Double.class) return clazz.cast(Double.valueOf(value));

        return JSON.parseObject(value, clazz);
    }

    /**
     * 设置单值 Key
     */
    public void setValue(String key, Object value) {
        redisTemplate.opsForValue().set(key, JSON.toJSONString(value));
    }

    // ===================== opsForHash =====================
    /**
     * 获取整个 Hash
     */
    public <T> Map<String, T> getHashAll(String key, Class<T> clazz) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        if (entries == null || entries.isEmpty()) return Map.of();

        return entries.entrySet().stream().collect(
                java.util.stream.Collectors.toMap(
                        e -> e.getKey().toString(),
                        e -> JSON.parseObject(e.getValue().toString(), clazz)
                )
        );
    }

    /**
     * 获取 Hash 指定字段
     */
    public <T> T getHashField(String key, String field, Class<T> clazz) {
        Object value = redisTemplate.opsForHash().get(key, field);
        if (value == null) return null;
        return JSON.parseObject(value.toString(), clazz);
    }

    /**
     * 设置 Hash 字段
     */
    public void setHashField(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, JSON.toJSONString(value));
    }

    // ===================== opsForList =====================
    /**
     * 获取 List 所有元素
     */
    public <T> List<T> getList(String key, Class<T> clazz) {
        List<String> list = redisTemplate.opsForList().range(key, 0, -1);
        if (list == null || list.isEmpty()) return List.of();
        return list.stream().map(s -> JSON.parseObject(s, clazz)).toList();
    }

    /**
     * 添加元素到 List
     */
    public void pushToList(String key, Object value) {
        redisTemplate.opsForList().rightPush(key, JSON.toJSONString(value));
    }

    // ===================== opsForZSet =====================
    /**
     * 获取 ZSet 所有元素（按分数排序）
     */
    public <T> List<T> getZSetAll(String key, Class<T> clazz) {
        var set = redisTemplate.opsForZSet().range(key, 0, -1);
        if (set == null || set.isEmpty()) return List.of();
        return set.stream().map(s -> JSON.parseObject(s.toString(), clazz)).toList();
    }

    /**
     * 添加元素到 ZSet
     */
    public void addToZSet(String key, Object value, double score) {
        redisTemplate.opsForZSet().add(key, JSON.toJSONString(value), score);
    }
}