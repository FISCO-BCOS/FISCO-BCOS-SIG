package com.trace.service.impl;

import cn.hutool.json.JSONUtil;
import com.trace.mapper.*;
import com.trace.model.*;
import com.trace.model.bo.ProductCreateDTO;
import com.trace.model.vo.*;
import com.trace.service.IProductService;
import com.trace.utils.JwtUtil;
import com.trace.utils.QRCodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductServiceImpl implements IProductService {

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
    private QRCodeUtil qrCodeUtil;
    @Autowired
    private com.trace.service.IBcosService bcosService;
    @Autowired
    private UserMapper userMapper;

    @Value("${system.contract.owner_address}")
    private String ownerAddress;

    @Value("${system.contract.traceAddress}")
    private String traceContractAddress;

    @Override
    @Transactional
    public Result<CreateProductResultVO> createProduct(ProductCreateDTO dto, String operatorUsername) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String datePrefix = "P" + dateStr;
        int seq = productMapper.countByDate(datePrefix) + 1;
        String productNo = datePrefix + String.format("%04d", seq);

        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        product.setProductNo(productNo);
        product.setStatus(0);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());

        // 设置创建者ID（farmerId）
        if (product.getFarmerId() == null) {
            User user = userMapper.findByUsername(operatorUsername);
            if (user != null) {
                product.setFarmerId(user.getId());
            }
        }

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            product.setImages(JSONUtil.toJsonStr(dto.getImages()));
        }

        String qrContent = qrCodeUtil.generateQRCodeContent(productNo, traceContractAddress, 1);
        product.setTraceQrcode(qrContent);

        productMapper.insert(product);

        // 上链并保存交易哈希
        try {
            Map<String, Object> chainResult = bcosService.createProduct(ownerAddress, productNo);
            log.info("createProduct on chain result: {}", chainResult);
            Object txHash = chainResult != null ? chainResult.get("transactionHash") : null;
            if (txHash != null) {
                product.setTxHash(txHash.toString());
                productMapper.update(product);
            }
        } catch (Exception e) {
            log.warn("createProduct on chain failed: {}", e.getMessage());
        }

        String qrBase64 = qrCodeUtil.generateQRCodeBase64(qrContent, 300, 300);

        CreateProductResultVO result = new CreateProductResultVO();
        result.setProductId(product.getId());
        result.setProductNo(productNo);
        result.setTraceQrcode(qrContent);
        result.setQrcodeBase64(qrBase64);

        return Result.success(result);
    }

    @Override
    public Result<Map<String, Object>> listProducts(int pageNum, int pageSize, String keyword, String category, Integer status, Long userId, String sortBy, String sortOrder) {
        int offset = (pageNum - 1) * pageSize;
        int total = productMapper.countByCondition(keyword, category, status, userId);
        List<Product> products = productMapper.findByCondition(keyword, category, status, userId, offset, pageSize, sortBy, sortOrder);
        int pages = (int) Math.ceil((double) total / pageSize);

        List<ProductVO> voList = products.stream().map(p -> {
            ProductVO vo = new ProductVO();
            BeanUtils.copyProperties(p, vo);
            vo.setCreateTime(p.getCreateTime() != null ? p.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
            vo.setScanCount(scanRecordMapper.countByProductId(p.getId()));
            return vo;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("pages", pages);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        result.put("list", voList);
        return Result.success(result);
    }

    @Override
    public Result<ProductDetailVO> getProductDetail(Long id) {
        Product product = productMapper.findById(id);
        if (product == null) {
            return Result.error(ResultVO.QUERY_EMPTY);
        }

        ProductVO productVO = new ProductVO();
        BeanUtils.copyProperties(product, productVO);
        productVO.setCreateTime(product.getCreateTime() != null ? product.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        productVO.setScanCount(scanRecordMapper.countByProductId(id));

        ProductDetailVO detail = new ProductDetailVO();
        detail.setProduct(productVO);

        List<TracePlanting> plantings = plantingMapper.findByProductId(id);
        detail.setPlanting(plantings != null && !plantings.isEmpty() ? plantings.get(0) : null);

        List<TraceProcessing> processings = processingMapper.findByProductId(id);
        detail.setProcessing(processings != null && !processings.isEmpty() ? processings.get(0) : null);

        List<TraceTesting> testings = testingMapper.findByProductId(id);
        detail.setTesting(testings != null && !testings.isEmpty() ? testings.get(0) : null);

        List<TraceLogistics> logistics = logisticsMapper.findByProductId(id);
        detail.setLogistics(logistics != null && !logistics.isEmpty() ? logistics.get(0) : null);

        detail.setScanCount(productVO.getScanCount());

        boolean complete = plantings != null && !plantings.isEmpty()
                && processings != null && !processings.isEmpty()
                && testings != null && !testings.isEmpty()
                && logistics != null && !logistics.isEmpty();
        detail.setIsComplete(complete);

        return Result.success(detail);
    }

    @Override
    @Transactional
    public Result<String> updateProduct(Long id, ProductCreateDTO dto) {
        Product product = productMapper.findById(id);
        if (product == null) {
            return Result.error(ResultVO.QUERY_EMPTY);
        }

        product.setProductName(dto.getProductName());
        product.setCategory(dto.getCategory());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setUnit(dto.getUnit());
        product.setOriginAddress(dto.getOriginAddress());
        if (dto.getFarmerId() != null) product.setFarmerId(dto.getFarmerId());
        if (dto.getProcessorId() != null) product.setProcessorId(dto.getProcessorId());
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            product.setImages(JSONUtil.toJsonStr(dto.getImages()));
        }
        product.setUpdateTime(LocalDateTime.now());

        productMapper.update(product);
        return Result.success("更新成功");
    }

    @Override
    public Result<DashboardVO> getDashboard() {
        DashboardVO vo = new DashboardVO();
        vo.setTotalProducts(productMapper.countTotal());
        vo.setTodayNew(productMapper.countTodayNew(java.time.LocalDate.now().toString() + " 00:00:00"));
        vo.setPendingRecords(productMapper.countPendingRecords());
        vo.setTotalScans(productMapper.countTotalScans());

        List<Product> latestProducts = productMapper.findLatest(10);
        List<ProductVO> voList = latestProducts.stream().map(p -> {
            ProductVO pv = new ProductVO();
            BeanUtils.copyProperties(p, pv);
            pv.setCreateTime(p.getCreateTime() != null ? p.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
            pv.setScanCount(scanRecordMapper.countByProductId(p.getId()));
            return pv;
        }).collect(Collectors.toList());
        vo.setLatestProducts(voList);

        return Result.success(vo);
    }

    @Override
    public Result<String> updateStatus(Long id, String status) {
        Product product = productMapper.findById(id);
        if (product == null) {
            return Result.error(ResultVO.QUERY_EMPTY);
        }
        Integer statusCode;
        switch (status) {
            case "已上架": statusCode = 1; break;
            case "已下架": statusCode = 2; break;
            case "草稿": statusCode = 0; break;
            default:
                try {
                    statusCode = Integer.parseInt(status);
                } catch (NumberFormatException e) {
                    return new Result<>(400, "无效的状态值: " + status, null);
                }
        }
        productMapper.updateStatus(id, statusCode);
        return Result.success("状态更新成功");
    }
}
