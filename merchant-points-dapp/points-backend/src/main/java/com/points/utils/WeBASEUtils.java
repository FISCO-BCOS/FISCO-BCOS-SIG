package com.points.utils;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.points.config.BcosConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class WeBASEUtils {

    @Autowired
    private BcosConfig bcosConfig;

    private String contractAbi;
    private static final String ABI_PATH = "abi/PointsContract.abi";

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource(ABI_PATH);
            contractAbi = new String(IoUtil.readBytes(resource.getInputStream()), StandardCharsets.UTF_8);
            log.info("合约ABI加载成功");
        } catch (IOException e) {
            log.error("合约ABI加载失败: {}", ABI_PATH, e);
            contractAbi = "[]";
        }
    }

    public JSONObject callContract(String userAddress, String funcName, List<Object> funcParam) {
        JSONObject body = JSONUtil.createObj()
                .set("groupId", String.valueOf(bcosConfig.getGroupId()))
                .set("user", userAddress)
                .set("contractAddress", bcosConfig.getContractAddress().getPoints())
                .set("funcName", funcName)
                .set("funcParam", JSONUtil.parseArray(JSONUtil.toJsonStr(funcParam)))
                .set("contractAbi", JSONUtil.parseArray(contractAbi))
                .set("useAes", false)
                .set("useCns", false)
                .set("cnsName", "");

        String json = body.toStringPretty();
        log.debug("WeBASE调用: func={}, params={}", funcName, funcParam);

        try {
            HttpResponse response = HttpRequest.post(bcosConfig.getWebaseUrl())
                    .header("Content-Type", "application/json;charset=utf-8")
                    .timeout(30000)
                    .body(json)
                    .execute();

            String respStr = response.body();
            log.debug("WeBASE返回: {}", respStr);

            String trimmed = respStr.trim();
            if (trimmed.startsWith("[")) {
                JSONArray arr = JSONUtil.parseArray(trimmed);
                JSONObject wrapper = new JSONObject();
                wrapper.set("code", 0);
                JSONObject data = new JSONObject();
                if (arr.size() > 0) {
                    data.set("output", arr.getStr(0));
                }
                wrapper.set("data", data);
                return wrapper;
            }

            JSONObject respJson = JSONUtil.parseObj(trimmed);
            Integer code = respJson.getInt("code");
            if (code == null || code != 0) {
                String errMsg = respJson.getStr("errorMessage", respJson.getStr("message", "合约调用失败"));
                log.warn("合约调用失败: func={}, code={}, message={}", funcName, code, errMsg);
            }
            return respJson;
        } catch (Exception e) {
            log.error("WeBASE调用异常: func={}", funcName, e);
            JSONObject errorResp = new JSONObject();
            errorResp.set("code", -1);
            errorResp.set("errorMessage", "WeBASE调用异常: " + e.getMessage());
            return errorResp;
        }
    }

    public boolean parseBoolOutput(JSONObject respJson) {
        JSONObject data = respJson.getJSONObject("data");
        if (data == null) return false;
        String output = data.getStr("output", "0x");
        if (StrUtil.isEmpty(output) || "0x".equals(output)) {
            return false;
        }
        return output.contains("0000000000000000000000000000000000000000000000000000000000000001");
    }

    public String getTxHash(JSONObject respJson) {
        JSONObject data = respJson.getJSONObject("data");
        if (data != null && data.containsKey("transactionHash")) {
            return data.getStr("transactionHash", "");
        }
        return respJson.getStr("transactionHash", "");
    }

    public Long getBlockNumber(JSONObject respJson) {
        JSONObject data = respJson.getJSONObject("data");
        String blockNum = "0";
        if (data != null && data.containsKey("blockNumber")) {
            blockNum = data.getStr("blockNumber", "0");
        } else {
            blockNum = respJson.getStr("blockNumber", "0");
        }
        try {
            return Long.parseLong(blockNum);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public String getMessage(JSONObject respJson) {
        return respJson.getStr("errorMessage", respJson.getStr("message", ""));
    }

    public boolean isSuccess(JSONObject respJson) {
        Integer code = respJson.getInt("code");
        return code != null && code == 0;
    }

    public String getOutput(JSONObject respJson) {
        JSONObject data = respJson.getJSONObject("data");
        if (data != null) {
            return data.getStr("output", "0x");
        }
        return respJson.getStr("output", "0x");
    }
}