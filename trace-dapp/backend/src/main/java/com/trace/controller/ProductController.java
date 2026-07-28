package com.trace.controller;

import com.trace.model.Result;
import com.trace.model.bo.ProductCreateDTO;
import com.trace.model.bo.StatusUpdateDTO;
import com.trace.model.vo.CreateProductResultVO;
import com.trace.model.vo.ProductDetailVO;
import com.trace.mapper.*;
import com.trace.model.TracePlanting;
import com.trace.model.TraceProcessing;
import com.trace.model.TraceTesting;
import com.trace.model.TraceLogistics;
import com.trace.service.IProductService;
import com.trace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RequestMapping("/api/products")
@RestController
public class ProductController {

    @Autowired
    private IProductService productService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PlantingMapper plantingMapper;
    @Autowired
    private ProcessingMapper processingMapper;
    @Autowired
    private TestingMapper testingMapper;
    @Autowired
    private LogisticsMapper logisticsMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private com.trace.mapper.UserMapper userMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("")
    public Result<CreateProductResultVO> create(@Valid @RequestBody ProductCreateDTO dto, HttpServletRequest request) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        String username = jwtUtil.getUsernameFromToken(token);
        return productService.createProduct(dto, username);
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "all") String scope,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            HttpServletRequest request) {
        Long userId = null;
        if ("mine".equals(scope)) {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                String tokenStr = token.substring(7);
                userId = jwtUtil.getUserIdFromToken(tokenStr);
                // 兼容老JWT（无userId字段）：通过username反查userId
                if (userId == null) {
                    String username = jwtUtil.getUsernameFromToken(tokenStr);
                    if (username != null) {
                        com.trace.model.User user = userMapper.findByUsername(username);
                        if (user != null) {
                            userId = user.getId();
                        }
                    }
                }
            }
        }
        return productService.listProducts(pageNum, pageSize, keyword, category, status, userId, sortBy, sortOrder);
    }

    @GetMapping("/{id}")
    public Result<ProductDetailVO> detail(@PathVariable Long id) {
        return productService.getProductDetail(id);
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @Valid @RequestBody ProductCreateDTO dto) {
        return productService.updateProduct(id, dto);
    }

    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Long id, @RequestBody StatusUpdateDTO dto) {
        return productService.updateStatus(id, dto.getStatus());
    }

    @GetMapping("/{id}/planting")
    public Result<List<Map<String, Object>>> plantingRecords(@PathVariable Long id) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (TracePlanting p : plantingMapper.findByProductId(id)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("baseName", p.getBaseName());
            m.put("variety", p.getCropVariety());
            m.put("sowingDate", p.getSowDate());
            m.put("area", p.getAreaSize());
            try {
                String farmOps = p.getFarmOps();
                if (farmOps != null && !farmOps.isEmpty()) {
                    List<?> rawList = objectMapper.readValue(farmOps, List.class);
                    m.put("farmingOps", rawList);
                } else {
                    m.put("farmingOps", new ArrayList<>());
                }
            } catch (Exception e) { m.put("farmingOps", new ArrayList<>()); }
            result.add(m);
        }
        return Result.success(result);
    }

    @GetMapping("/{id}/processing")
    public Result<List<Map<String, Object>>> processingRecords(@PathVariable Long id) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (TraceProcessing p : processingMapper.findByProductId(id)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("stepName", "加工");
            m.put("description", p.getProcessSteps() != null ? p.getProcessSteps() : "");
            m.put("operator", "");
            m.put("processDate", p.getCreateTime() != null ? p.getCreateTime().toString().substring(0, 10) : "");
            result.add(m);
        }
        return Result.success(result);
    }

    @GetMapping("/{id}/testing")
    public Result<Map<String, Object>> testingRecords(@PathVariable Long id) {
        List<TraceTesting> records = testingMapper.findByProductId(id);
        List<Map<String, Object>> list = new ArrayList<>();
        List<Map<String, Object>> items = new ArrayList<>();
        for (TraceTesting t : records) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.getId());
            m.put("reportName", "检测报告-" + t.getTestingNo());
            m.put("testingOrg", "检测机构");
            m.put("testingDate", t.getTestDate());
            m.put("result", t.getTestResult() != null && t.getTestResult() == 1 ? "合格" : "不合格");
            list.add(m);
            if (t.getTestItems() != null && !t.getTestItems().isEmpty()) {
                try {
                    List<?> rawItems = objectMapper.readValue(t.getTestItems(), List.class);
                    items.addAll((List) rawItems);
                } catch (Exception ignored) {}
            }
        }
        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put("records", list);
        wrapper.put("items", items);
        return Result.success(wrapper);
    }

    @GetMapping("/{id}/logistics")
    public Result<List<Map<String, Object>>> logisticsRecords(@PathVariable Long id) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (TraceLogistics l : logisticsMapper.findByProductId(id)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", l.getId());
            m.put("type", l.getTransportMode() != null ? l.getTransportMode() : "运输");
            m.put("location", "");
            m.put("description", l.getVehicleInfo() != null ? l.getVehicleInfo() : "");
            m.put("operator", "");
            m.put("operateTime", l.getCreateTime() != null ? l.getCreateTime().toString().substring(0, 16).replace('T', ' ') : "");
            result.add(m);
        }
        return Result.success(result);
    }

    /**
     * 补填老产品的farmer_id（用于已有数据迁移）
     */
    @PostMapping("/backfill-farmer")
    public Result<String> backfillFarmer(HttpServletRequest request) {
        Long userId = null;
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            userId = jwtUtil.getUserIdFromToken(token.substring(7));
        }
        // 如果token不含userId，从username获取（兼容老token）
        if (userId == null) {
            String username = jwtUtil.getUsernameFromToken(token.substring(7));
            com.trace.model.User user = userMapper.findByUsername(username);
            if (user != null) userId = user.getId();
        }
        if (userId != null) {
            int updated = productMapper.updateNullFarmerId(userId);
            return Result.success("已更新 " + updated + " 条产品记录");
        }
        return Result.success("未找到用户信息");
    }
}