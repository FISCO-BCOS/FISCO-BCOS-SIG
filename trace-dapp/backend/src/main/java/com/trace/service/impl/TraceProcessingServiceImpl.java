package com.trace.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONUtil;
import com.trace.exception.BusinessException;
import com.trace.mapper.ProcessingMapper;
import com.trace.mapper.ProductMapper;
import com.trace.model.Product;
import com.trace.model.Result;
import com.trace.model.TraceProcessing;
import com.trace.model.bo.TraceProcessingDTO;
import com.trace.model.vo.ResultVO;
import com.trace.service.IBcosService;
import com.trace.service.ITraceProcessingService;
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
public class TraceProcessingServiceImpl implements ITraceProcessingService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ProcessingMapper processingMapper;
    @Autowired
    private IBcosService bcosService;

    @Value("${system.contract.owner_address}")
    private String ownerAddress;

    @Override
    @Transactional
    public Result<String> recordProcessing(TraceProcessingDTO dto, String username) {
        if (dto.getProductId() == null || dto.getProcessSteps() == null || dto.getProcessSteps().isEmpty()) {
            return Result.error(ResultVO.PARAM_EMPTY);
        }

        Product product = productMapper.findById(dto.getProductId());
        if (product == null) {
            return Result.error(ResultVO.QUERY_EMPTY);
        }

        String processingNo = "PR" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", RandomUtil.randomInt(0, 9999));

        TraceProcessing processing = new TraceProcessing();
        BeanUtils.copyProperties(dto, processing);
        processing.setProcessingNo(processingNo);
        processing.setProcessSteps(JSONUtil.toJsonStr(dto.getProcessSteps()));
        processing.setChainStatus(0);
        processing.setCreateTime(LocalDateTime.now());

        String dataHash = SecureUtil.sha256(JSONUtil.toJsonStr(dto.getProcessSteps()));

        try {
            Map<String, Object> chainResult = bcosService.recordProcessing(
                    ownerAddress, processingNo, product.getProductNo(), dataHash, username);

            String txHash = (String) chainResult.get("transactionHash");
            Object blockNumberObj = chainResult.get("blockNumber");
            BigInteger blockNumber = blockNumberObj != null ? new BigInteger(blockNumberObj.toString()) : BigInteger.ZERO;

            processing.setTxHash(txHash);
            processing.setBlockNumber(blockNumber);
            processing.setChainStatus(1);
        } catch (Exception e) {
            log.error("加工上链失败, processingNo={}", processingNo, e);
            throw new BusinessException(ResultVO.CONTRACT_ERROR.getCode(), "加工上链失败: " + e.getMessage());
        }

        processingMapper.insert(processing);
        log.info("加工记录创建成功, processingNo={}, productId={}", processingNo, dto.getProductId());
        return Result.success(processingNo);
    }
}