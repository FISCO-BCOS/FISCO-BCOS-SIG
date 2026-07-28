package com.evidence.blockchain;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

@Service
public class WeBASEUtils {

    private static final Logger log = LoggerFactory.getLogger(WeBASEUtils.class);

    @Value("${webase.url}")
    String webaseUrl;

    @Value("${fisco.bcos.contract.evidence-address}")
    String contractAddress;

    public static final String ABI = com.evidence.utils.IOUtil.readResourceAsString("abi/EvidenceContract.abi");

    public String funcPost(String userAddress, String funcName, List<?> funcParam) {
        JSONArray abiJSON = JSONUtil.parseArray(ABI);
        JSONObject data = JSONUtil.createObj();
        data.set("groupId", "1");
        data.set("contractPath", "/");
        data.set("contractAbi", abiJSON);
        data.set("useAes", false);
        data.set("useCns", false);
        data.set("cnsName", "");

        data.set("user", userAddress);
        data.set("contractAddress", contractAddress);
        data.set("funcName", funcName);
        data.set("funcParam", funcParam);

        String dataString = JSONUtil.toJsonStr(data);

        HttpPost httpPost = new HttpPost(webaseUrl);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(10000)
                .setConnectionRequestTimeout(10000)
                .setSocketTimeout(30000)
                .build();
        httpPost.setConfig(requestConfig);
        httpPost.setHeader("Content-type", "application/json;charset=utf-8");
        StringEntity entity = new StringEntity(dataString, Charset.forName("UTF-8"));
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);

        String result = null;
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
            result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            if (httpResponse.getStatusLine().getStatusCode() >= 400) {
                log.warn("WeBASE call returned HTTP {}: func={}, response={}",
                        httpResponse.getStatusLine().getStatusCode(), funcName, result);
            }
        } catch (IOException e) {
            log.error("WeBASE call failed: func={}, error={}", funcName, e.getMessage());
        }
        log.info("WeBASE response: func={}, result={}", funcName, result);
        return result;
    }
}
