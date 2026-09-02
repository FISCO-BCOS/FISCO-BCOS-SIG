package com.trace.utils;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WeBASEUtils {

    private static String webaseUrl;
    private static String contractAddress;
    private static final String ABI = IOUtil.readResourceAsString("abi/TraceabilityContract.abi");

    @Value("${project.webase-url}")
    public void setWebaseUrl(String url) {
        webaseUrl = url;
    }

    @Value("${system.contract.traceAddress}")
    public void setContractAddress(String address) {
        contractAddress = address;
    }

    /**
     * 发送合约调用请求（统一入口）
     * 写操作返回: {"code":0,"message":"Success",...}
     * 读操作返回: ["{innerJsonString}"]
     */
    public static String funcPost(String userAddress, String funcName, List<Object> funcParam) {
        try {
            cn.hutool.json.JSONObject json = new cn.hutool.json.JSONObject();
            json.putOpt("groupId", 1);
            json.putOpt("contractPath", "/");
            json.putOpt("contractAbi", new cn.hutool.json.JSONArray(ABI));
            json.putOpt("user", userAddress);
            json.putOpt("contractAddress", contractAddress);
            json.putOpt("funcName", funcName);
            json.putOpt("funcParam", funcParam);

            HttpPost httpPost = new HttpPost(webaseUrl);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(10000)
                    .setConnectionRequestTimeout(10000)
                    .setSocketTimeout(30000)
                    .build();
            httpPost.setConfig(requestConfig);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(json.toString(), "UTF-8"));

            String result;
            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(httpPost)) {
                result = EntityUtils.toString(response.getEntity(), "UTF-8");
                if (response.getStatusLine().getStatusCode() >= 400) {
                    throw new RuntimeException("WeBASE HTTP " + response.getStatusLine().getStatusCode()
                            + ": " + result);
                }
            }

            // 打印原始响应（前300字符）
            log.info("WeBASE {} RAW ({} chars): {}", funcName, result.length(),
                     result.substring(0, Math.min(300, result.length())));

            // 标准化响应格式
            result = normalizeResponse(result);
            log.info("WeBASE {} NORM ({} chars): {}", funcName, result.length(),
                     result.substring(0, Math.min(300, result.length())));

            return result;
        } catch (Exception e) {
            throw new RuntimeException("WeBASE request failed: " + e.getMessage(), e);
        }
    }

    /**
     * 标准化WeBASE响应：
     * 1. 读操作返回 ["{...}"] → 提取内层 {...}
     * 2. 内层可能有 :null 值 → 替换为 :""
     * 3. 清理 receiptProof 的 JSONNull 问题
     */
    static String normalizeResponse(String raw) {
        if (raw == null || raw.trim().isEmpty()) return raw;
        raw = raw.trim();

        // 检测是否是数组格式（读操作的典型特征）
        if (raw.startsWith("[")) {
            try {
                cn.hutool.json.JSONArray arr = JSONUtil.parseArray(raw);
                if (!arr.isEmpty() && arr.getStr(0) != null) {
                    String inner = arr.getStr(0);
                    // hutool解析后，字符串中的\"已被反转义为"
                    // 但可能包含未引号包裹的null值，需要修复
                    inner = fixUnquotedNulls(inner);
                    log.debug("Extracted inner JSON ({} chars): {}", inner.length(),
                              inner.substring(0, Math.min(150, inner.length())));
                    return inner;
                }
            } catch (Exception ignored) { /* 非数组格式 */ }
        }

        // 写操作：修复可能的null值问题
        return fixUnquotedNulls(raw);
    }

    /**
     * 修复WeBASE返回的非标准JSON
     * 问题1: "key":"key":{value}  →  "key":{value}  (struct被序列化为字符串)
     * 问题2: :null              →  :""            (未引号包裹的null)
     */
    private static String fixUnquotedNulls(String json) {
        // 先修复 struct-as-string 问题: "fieldName":"fieldName":{...} → "fieldName":{...}
        json = json.replaceAll("\"(\\w+)\":\"\\1\":\\s*\\{", "\"$1\":{");
        // 同样处理结尾没有大括号的: "fieldName":"fieldName":null → "fieldName":null（后面统一处理null）
        json = json.replaceAll("\"(\\w+)\":\"\\1\":null", "\"$1\":null");

        // 再修复 :null → :""
        StringBuilder sb = new StringBuilder(json.length());
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) { sb.append(c); escaped = false; continue; }
            if (c == '\\' && inString) { sb.append(c); escaped = true; continue; }
            if (c == '"') { inString = !inString; sb.append(c); continue; }
            // 不在字符串内部时，检查 :null 模式
            if (!inString && c == 'n' && i + 3 < json.length()
                && json.charAt(i + 1) == 'u' && json.charAt(i + 2) == 'l' && json.charAt(i + 3) == 'l'
                && (i + 4 >= json.length() || ",}".indexOf(json.charAt(i + 4)) >= 0)) {
                int prevNonSpace = sb.length() - 1;
                while (prevNonSpace >= 0 && sb.charAt(prevNonSpace) == ' ') prevNonSpace--;
                if (prevNonSpace >= 0 && sb.charAt(prevNonSpace) == ':') {
                    sb.append("\"\"");
                    i += 3;
                    continue;
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
