package com.trace.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import com.trace.model.*;
import com.trace.model.vo.ResultVO;
import com.trace.service.IBcosService;
import com.trace.service.ITraceTestingService;
import com.trace.mapper.ProductMapper;
import com.trace.mapper.TestingMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
public class TraceTestingServiceImpl implements ITraceTestingService {

    @Autowired private TestingMapper testingMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private IBcosService bcosService;
    @Value("${system.contract.owner_address}") private String ownerAddress;
    @Value("${file.upload-path:./static/uploads/}") private String uploadPath;

    @Override
    public Result<String> recordTesting(Long productId, String testItems, Integer testResult, String testDate, MultipartFile reportFile, String username) {
        Product product = productMapper.findById(productId);
        if (product == null) return Result.error(ResultVO.QUERY_EMPTY);

        String testingNo = "TE" + DateUtil.format(new Date(), "yyyyMMddHHmmss") + IdUtil.fastSimpleUUID().substring(0, 4).toUpperCase();

        String reportFileUrl = null;
        if (reportFile != null && !reportFile.isEmpty()) {
            try {
                String ext = "";
                String originalName = reportFile.getOriginalFilename();
                if (originalName != null && originalName.contains(".")) {
                    ext = originalName.substring(originalName.lastIndexOf("."));
                }
                String newName = IdUtil.fastSimpleUUID() + ext;
                String dirPath = uploadPath + "reports/";
                File dir = new File(dirPath);
                if (!dir.exists()) dir.mkdirs();
                File dest = new File(dirPath + newName);
                reportFile.transferTo(dest);
                reportFileUrl = "/uploads/reports/" + newName;
            } catch (Exception e) {
                return Result.error(ResultVO.CONTRACT_ERROR);
            }
        }

        String dataHash = SecureUtil.sha256(testItems + (reportFileUrl != null ? reportFileUrl : ""));
        try {
            Map<String, Object> chainResult = bcosService.recordTesting(ownerAddress, testingNo, product.getProductNo(), dataHash, username);
            log.info("recordTesting on chain result: {}", chainResult);
        } catch (Exception e) {
            log.warn("recordTesting on chain failed: {}", e.getMessage());
        }

        TraceTesting entity = new TraceTesting();
        entity.setTestingNo(testingNo);
        entity.setProductId(productId);
        entity.setTesterId(null);
        entity.setTestItems(testItems);
        entity.setTestResult(testResult);
        entity.setReportFileUrl(reportFileUrl);
        entity.setTestDate(testDate);
        entity.setTxHash("");
        entity.setBlockNumber(null);
        entity.setChainStatus(1);
        entity.setCreateTime(LocalDateTime.now());
        testingMapper.insert(entity);

        return Result.success("ok");
    }
}
