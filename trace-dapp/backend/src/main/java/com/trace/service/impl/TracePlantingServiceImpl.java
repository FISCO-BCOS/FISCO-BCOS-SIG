package com.trace.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONUtil;
import com.trace.exception.BusinessException;
import com.trace.mapper.PlantingMapper;
import com.trace.mapper.ProductMapper;
import com.trace.model.Product;
import com.trace.model.Result;
import com.trace.model.TracePlanting;
import com.trace.model.bo.TracePlantingDTO;
import com.trace.model.vo.ResultVO;
import com.trace.service.IBcosService;
import com.trace.service.ITracePlantingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Service
public class TracePlantingServiceImpl implements ITracePlantingService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private PlantingMapper plantingMapper;
    @Autowired
    private IBcosService bcosService;

    @Value("${system.contract.owner_address}")
    private String ownerAddress;

    @Override
    @Transactional
    public Result<String> recordPlanting(TracePlantingDTO dto, String username) {
        if (dto.getProductId() == null || dto.getFarmOps() == null || dto.getFarmOps().isEmpty()) {
            return Result.error(ResultVO.PARAM_EMPTY);
        }

        Product product = productMapper.findById(dto.getProductId());
        if (product == null) {
            return Result.error(ResultVO.QUERY_EMPTY);
        }

        String plantingNo = "PL" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", RandomUtil.randomInt(0, 9999));

        TracePlanting planting = new TracePlanting();
        BeanUtils.copyProperties(dto, planting);
        planting.setPlantingNo(plantingNo);
        planting.setFarmOps(JSONUtil.toJsonStr(dto.getFarmOps()));
        planting.setChainStatus(0);
        planting.setCreateTime(LocalDateTime.now());

        String dataHash = SecureUtil.sha256(JSONUtil.toJsonStr(dto.getFarmOps()));

        try {
            Map<String, Object> chainResult = bcosService.recordPlanting(
                    ownerAddress, plantingNo, product.getProductNo(), dataHash, username);

            String txHash = (String) chainResult.get("transactionHash");
            Object blockNumberObj = chainResult.get("blockNumber");
            BigInteger blockNumber = blockNumberObj != null ? new BigInteger(blockNumberObj.toString()) : BigInteger.ZERO;

            planting.setTxHash(txHash);
            planting.setBlockNumber(blockNumber);
            planting.setChainStatus(1);
        } catch (Exception e) {
            log.error("种植上链失败, plantingNo={}", plantingNo, e);
            throw new BusinessException(ResultVO.CONTRACT_ERROR.getCode(), "种植上链失败: " + e.getMessage());
        }

        plantingMapper.insert(planting);
        log.info("种植记录创建成功, plantingNo={}, productId={}", plantingNo, dto.getProductId());
        return Result.success(plantingNo);
    }
}