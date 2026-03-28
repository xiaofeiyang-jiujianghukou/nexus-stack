package com.nexus.stack.brain.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

@Slf4j
public class SqlGatewayClient {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String createSession(String sqlGatewayUrl) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(sqlGatewayUrl + "/v1/sessions");
            post.setHeader("Content-Type", "application/json");

            try (CloseableHttpResponse response = client.execute(post)) {
                String result = EntityUtils.toString(response.getEntity());
                JsonNode json = mapper.readTree(result);
                return json.get("sessionHandle").asText();
            }
        }
    }

    public static String executeSql(String sessionHandle, String sqlGatewayUrl, String sql, String sqlName) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost executeStmt = new HttpPost(
                    sqlGatewayUrl + "/v1/sessions/" + sessionHandle + "/statements");
            executeStmt.setHeader("Content-Type", "application/json");

            String escapedSql = sql.replace("\"", "\\\"").replace("\n", "\\n");
            String requestBody = "{\"statement\": \"" + escapedSql + "\"}";
            executeStmt.setEntity(new StringEntity(requestBody));

            try (CloseableHttpResponse stmtResp = client.execute(executeStmt)) {
                String stmtResult = EntityUtils.toString(stmtResp.getEntity());
                JsonNode json = mapper.readTree(stmtResult);
                if (json.has("operationHandle")) {
                    return json.get("operationHandle").asText();
                } else if (json.has("errors")) {
                    throw new RuntimeException(sqlName + " SQL执行失败: " + stmtResult);
                }
                return stmtResult;
            }
        }
    }
}
