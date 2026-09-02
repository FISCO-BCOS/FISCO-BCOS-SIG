package com.trace.service.impl;

import com.trace.mapper.*;
import com.trace.model.*;
import com.trace.model.vo.ProductVO;
import com.trace.model.vo.ResultVO;
import com.trace.model.vo.TraceQueryResultVO;
import com.trace.service.IBcosService;
import com.trace.service.ITraceQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TraceQueryServiceImpl implements ITraceQueryService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private PlantingMapper plantingMapper;
    @Autowired
    private ProcessingMapper processingMapper;
    @Autowired
    private TestingMapper testingMapper;
    @Autowired
    private LogisticsMapper logisticsMapper;
    @Autowired
    private ScanRecordMapper scanRecordMapper;
    @Autowired
    private IBcosService bcosService;

    @Value("${system.contract.owner_address}")
    private String ownerAddress;

    private TraceQueryResultVO assembleTraceQuery(Product product) {
        TraceQueryResultVO vo = new TraceQueryResultVO();

        ProductVO productVO = new ProductVO();
        BeanUtils.copyProperties(product, productVO);
        if (product.getCreateTime() != null) {
            productVO.setCreateTime(product.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        productVO.setScanCount(scanRecordMapper.countByProductId(product.getId()));
        vo.setProduct(productVO);

        List<TracePlanting> plantings = plantingMapper.findByProductId(product.getId());
        vo.setPlanting(plantings != null && !plantings.isEmpty() ? plantings.get(0) : null);

        List<TraceProcessing> processings = processingMapper.findByProductId(product.getId());
        vo.setProcessing(processings != null && !processings.isEmpty() ? processings.get(0) : null);

        List<TraceTesting> testings = testingMapper.findByProductId(product.getId());
        vo.setTesting(testings != null && !testings.isEmpty() ? testings.get(0) : null);

        List<TraceLogistics> logistics = logisticsMapper.findByProductId(product.getId());
        vo.setLogistics(logistics != null && !logistics.isEmpty() ? logistics.get(0) : null);

        List<TraceScanRecord> scanRecords = scanRecordMapper.findByProductId(product.getId());
        vo.setScanRecords(scanRecords);
        vo.setScanCount(scanRecords.size());

        try {
            Map<String, Object> chainData = bcosService.getTraceRecord(product.getProductNo());
            vo.setChainData(chainData);
            vo.setChainVerified(true);
        } catch (Exception e) {
            log.warn("链上数据查询失败, productNo={}", product.getProductNo(), e);
            vo.setChainVerified(false);
        }

        return vo;
    }

    @Override
    @Transactional
    public Result<TraceQueryResultVO> queryByProductNo(String productNoOrQrcode) {
        String productNo;
        if (productNoOrQrcode != null && productNoOrQrcode.startsWith("TRACE|")) {
            String[] parts = productNoOrQrcode.split("\\|");
            if (parts.length >= 2) {
                productNo = parts[1];
            } else {
                return Result.error(ResultVO.PARAM_EMPTY);
            }
        } else {
            productNo = productNoOrQrcode;
        }

        Product product = productMapper.findByProductNo(productNo);
        if (product == null) {
            return Result.error(ResultVO.QUERY_EMPTY);
        }

        TraceQueryResultVO vo = assembleTraceQuery(product);

        TraceScanRecord scanRecord = new TraceScanRecord();
        scanRecord.setProductId(product.getId());
        scanRecord.setScanType(0);
        scanRecord.setScanTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        scanRecord.setCreateTime(LocalDateTime.now());
        scanRecordMapper.insert(scanRecord);

        try {
            Map<String, Object> chainResult = bcosService.recordScan(ownerAddress, productNo);
            log.info("recordScan on chain result: {}", chainResult);
        } catch (Exception e) {
            log.warn("recordScan on chain failed, productNo={}", productNo, e);
        }

        log.info("溯源查询成功, productNo={}", productNo);
        return Result.success(vo);
    }

    @Override
    public Result<TraceQueryResultVO> queryById(Long productId) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            return Result.error(ResultVO.QUERY_EMPTY);
        }

        TraceQueryResultVO vo = assembleTraceQuery(product);
        return Result.success(vo);
    }
}