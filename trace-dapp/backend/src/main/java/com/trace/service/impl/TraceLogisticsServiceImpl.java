package com.trace.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONUtil;
import com.trace.model.*;
import com.trace.model.bo.TraceLogisticsDTO;
import com.trace.model.vo.ResultVO;
import com.trace.service.IBcosService;
import com.trace.service.ITraceLogisticsService;
import com.trace.mapper.ProductMapper;
import com.trace.mapper.LogisticsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
public class TraceLogisticsServiceImpl implements ITraceLogisticsService {

    @Autowired private LogisticsMapper logisticsMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private IBcosService bcosService;
    @Value("${system.contract.owner_address}") private String ownerAddress;

    @Override
    public Result<String> recordLogistics(TraceLogisticsDTO dto, String username) {
        Product product = productMapper.findById(dto.getProductId());
        if (product == null) return Result.error(ResultVO.QUERY_EMPTY);

        String logisticsNo = "LO" + DateUtil.format(new Date(), "yyyyMMddHHmmss") + IdUtil.fastSimpleUUID().substring(0, 4).toUpperCase();

        String logisticsNodesJson = JSONUtil.toJsonStr(dto.getLogisticsNodes());
        String dataHash = SecureUtil.sha256(logisticsNodesJson);

        try {
            Map<String, Object> chainResult = bcosService.recordLogistics(ownerAddress, logisticsNo, product.getProductNo(), dataHash, username);
            log.info("recordLogistics on chain result: {}", chainResult);
        } catch (Exception e) {
            log.warn("recordLogistics on chain failed: {}", e.getMessage());
        }

        TraceLogistics entity = new TraceLogistics();
        entity.setLogisticsNo(logisticsNo);
        entity.setProductId(dto.getProductId());
        entity.setLogisticsProviderId(null);
        entity.setTransportMode(dto.getTransportMode());
        entity.setVehicleInfo(dto.getVehicleInfo());
        entity.setLogisticsNodes(logisticsNodesJson);
        entity.setTxHash("");
        entity.setBlockNumber(null);
        entity.setChainStatus(1);
        entity.setCreateTime(LocalDateTime.now());
        logisticsMapper.insert(entity);

        return Result.success("ok");
    }
}
