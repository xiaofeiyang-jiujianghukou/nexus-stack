package com.nexus.stack.brain.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 通用方法：从 JSON 中获取指定字段的字符串值
     *
     * @param json      JSON 字符串
     * @param fieldName 要提取的字段名
     * @return 字段值，如果字段不存在或解析失败返回 null
     */
    public static String getStringField(String fieldName, String json) {
        if (json == null || fieldName == null) {
            return null;
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(json);
            JsonNode fieldNode = node.get(fieldName);
            if (fieldNode != null && !fieldNode.isNull()) {
                return fieldNode.asText();
            }
        } catch (Exception e) {
            // 可以根据需求 log.warn("解析 JSON 失败", e);
        }
        return null;
    }

    // 测试
    public static void main(String[] args) {
        String json = """
                {"sessionHandle":"c78c30b0-3bae-4bc9-8961-1fb9dde5c912",
                 "operationHandle":"959d67c7-ca57-4c60-a614-bd2831984543",
                 "count":123}
                """;

        System.out.println(getStringField(json, "sessionHandle"));   // 输出 UUID
        System.out.println(getStringField(json, "operationHandle")); // 输出 UUID
        System.out.println(getStringField(json, "count"));           // 输出数字 123
        System.out.println(getStringField(json, "notExist"));        // 输出 null
    }
}
