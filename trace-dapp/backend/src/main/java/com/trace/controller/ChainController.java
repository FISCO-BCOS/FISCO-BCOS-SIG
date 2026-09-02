package com.trace.controller;

import com.trace.mapper.*;
import com.trace.model.*;
import com.trace.model.vo.ChainRecordVO;
import com.trace.model.vo.OperationLogVO;
import com.trace.model.vo.ResultVO;
import com.trace.service.IBcosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
public class ChainController {

    @Autowired private PlantingMapper plantingMapper;
    @Autowired private ProcessingMapper processingMapper;
    @Autowired private TestingMapper testingMapper;
    @Autowired private LogisticsMapper logisticsMapper;
    @Autowired private ChainApprovalMapper approvalMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private IBcosService bcosService;

    @Value("${system.contract.owner_address}")
    private String ownerAddress;

    /** 角色常量 */
    private static final int ROLE_ADMIN = 0;
    private static final int ROLE_FARMER = 1;
    private static final int ROLE_PROCESSOR = 2;
    private static final int ROLE_TESTER = 3;
    private static final int ROLE_LOGISTICS = 4;

    @GetMapping("/api/chain/records")
    public Result<Map<String, Object>> chainRecords(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<ChainRecordVO> all = Stream.of(
                plantingMapper.findAll().stream().map(p -> {
                    ChainRecordVO vo = new ChainRecordVO();
                    vo.setRecordType("种植记录");
                    vo.setRecordNo(p.getPlantingNo());
                    vo.setTxHash(p.getTxHash());
                    vo.setChainStatus(p.getChainStatus());
                    vo.setCreateTime(p.getCreateTime() != null ? p.getCreateTime().format(fmt) : null);
                    return vo;
                }),
                processingMapper.findAll().stream().map(p -> {
                    ChainRecordVO vo = new ChainRecordVO();
                    vo.setRecordType("加工记录");
                    vo.setRecordNo(p.getProcessingNo());
                    vo.setTxHash(p.getTxHash());
                    vo.setChainStatus(p.getChainStatus());
                    vo.setCreateTime(p.getCreateTime() != null ? p.getCreateTime().format(fmt) : null);
                    return vo;
                }),
                testingMapper.findAll().stream().map(t -> {
                    ChainRecordVO vo = new ChainRecordVO();
                    vo.setRecordType("检测记录");
                    vo.setRecordNo(t.getTestingNo());
                    vo.setTxHash(t.getTxHash());
                    vo.setChainStatus(t.getChainStatus());
                    vo.setCreateTime(t.getCreateTime() != null ? t.getCreateTime().format(fmt) : null);
                    return vo;
                }),
                logisticsMapper.findAll().stream().map(l -> {
                    ChainRecordVO vo = new ChainRecordVO();
                    vo.setRecordType("物流记录");
                    vo.setRecordNo(l.getLogisticsNo());
                    vo.setTxHash(l.getTxHash());
                    vo.setChainStatus(l.getChainStatus());
                    vo.setCreateTime(l.getCreateTime() != null ? l.getCreateTime().format(fmt) : null);
                    return vo;
                })
        ).flatMap(s -> s)
         .sorted((a, b) -> {
             if (a.getCreateTime() == null) return 1;
             if (b.getCreateTime() == null) return -1;
             return b.getCreateTime().compareTo(a.getCreateTime());
         })
         .collect(Collectors.toList());

        int total = all.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<ChainRecordVO> page = fromIndex < total ? all.subList(fromIndex, toIndex) : Collections.emptyList();

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("pages", (int) Math.ceil((double) total / pageSize));
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        result.put("rows", page);
        return Result.success(result);
    }

    @GetMapping("/api/logs/operation")
    public Result<Map<String, Object>> operationLogs(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<OperationLogVO> all = Stream.of(
                plantingMapper.findAll().stream().map(p -> {
                    OperationLogVO vo = new OperationLogVO();
                    vo.setAction("种植信息录入");
                    vo.setDetail("种植编号: " + p.getPlantingNo());
                    vo.setTime(p.getCreateTime() != null ? p.getCreateTime().format(fmt) : null);
                    return vo;
                }),
                processingMapper.findAll().stream().map(p -> {
                    OperationLogVO vo = new OperationLogVO();
                    vo.setAction("加工信息录入");
                    vo.setDetail("加工编号: " + p.getProcessingNo());
                    vo.setTime(p.getCreateTime() != null ? p.getCreateTime().format(fmt) : null);
                    return vo;
                }),
                testingMapper.findAll().stream().map(t -> {
                    OperationLogVO vo = new OperationLogVO();
                    vo.setAction("检测信息录入");
                    vo.setDetail("检测编号: " + t.getTestingNo());
                    vo.setTime(t.getCreateTime() != null ? t.getCreateTime().format(fmt) : null);
                    return vo;
                }),
                logisticsMapper.findAll().stream().map(l -> {
                    OperationLogVO vo = new OperationLogVO();
                    vo.setAction("物流信息录入");
                    vo.setDetail("物流编号: " + l.getLogisticsNo());
                    vo.setTime(l.getCreateTime() != null ? l.getCreateTime().format(fmt) : null);
                    return vo;
                })
        ).flatMap(s -> s)
         .sorted((a, b) -> {
             if (a.getTime() == null) return 1;
             if (b.getTime() == null) return -1;
             return b.getTime().compareTo(a.getTime());
         })
         .collect(Collectors.toList());

        int total = all.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<OperationLogVO> page = fromIndex < total ? all.subList(fromIndex, toIndex) : Collections.emptyList();

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("pages", (int) Math.ceil((double) total / pageSize));
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        result.put("rows", page);
        return Result.success(result);
    }

    // ==================== 上链审批流程 ====================

    /**
     * 用户提交上链请求（带角色前置校验）
     */
    @PostMapping("/api/chain/submit")
    public Result<?> submitChain(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = getAuthenticatedUserId(request);
        Integer userRole = getAuthenticatedRole(request);
        String username = getAuthenticatedUsername(request);
        if (userId == null || userRole == null || username == null) {
            return Result.error(ResultVO.TOKEN_EMPTY);
        }

        Long productId = Long.valueOf(body.get("productId").toString());

        // 获取产品信息
        Product product = productMapper.findById(productId);
        if (product == null) {
            return Result.error(new com.trace.model.vo.ResultVO(404001, "产品不存在", "产品不存在"));
        }

        // 检查是否已有待审批的请求
        List<ChainApprovalRequest> existing = approvalMapper.findByProductId(productId);
        for (ChainApprovalRequest req : existing) {
            if (req.getStatus() == 0) {
                return Result.error(new com.trace.model.vo.ResultVO(400001, "该产品已有待审批的上链请求，请等待审核结果", "该产品已有待审批的上链请求"));
            }
        }

        // 角色前置校验
        String validationMsg = validateRoleRecords(productId, userRole);

        // 创建审批请求
        ChainApprovalRequest approval = new ChainApprovalRequest();
        approval.setProductId(productId);
        approval.setProductNo(product.getProductNo());
        approval.setUserId(userId);
        approval.setUserRole(userRole);
        approval.setUserName(username);
        approval.setStatus(0); // 待审批
        approval.setValidationMsg(validationMsg);
        approvalMapper.insert(approval);

        Map<String, Object> result = new HashMap<>();
        result.put("requestId", approval.getId());
        result.put("status", "pending");
        result.put("validationMsg", validationMsg);
        return Result.success(result);
    }

    /**
     * 用户取消自己的上链请求（仅待审批状态可取消）
     */
    @PutMapping("/api/chain/cancel/{id}")
    public Result<?> cancelChain(@PathVariable Long id, HttpServletRequest request) {
        ChainApprovalRequest req = approvalMapper.findById(id);
        if (req == null) {
            return Result.error(new ResultVO(404002, "审批请求不存在", "审批请求不存在"));
        }
        Long userId = getAuthenticatedUserId(request);
        Integer role = getAuthenticatedRole(request);
        if (userId == null || role == null) {
            return Result.error(ResultVO.TOKEN_EMPTY);
        }
        if (role != ROLE_ADMIN && !userId.equals(req.getUserId())) {
            return Result.error(ResultVO.FORBIDDEN);
        }
        if (req.getStatus() != 0) {
            return Result.error(new ResultVO(400002, "该请求已处理，无法取消", "该请求已处理，无法取消"));
        }
        req.setStatus(2); // 标记为已拒绝（取消）
        req.setRejectReason("申请人主动取消");
        approvalMapper.updateStatus(req);

        Map<String, Object> result = new HashMap<>();
        result.put("requestId", id);
        result.put("message", "上链请求已取消");
        return Result.success(result);
    }

    /**
     * 管理者查看审批列表（支持状态筛选）
     */
    @GetMapping("/api/chain/pending")
    public Result<List<ChainApprovalRequest>> pendingApprovals(
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error(ResultVO.FORBIDDEN);
        }
        if (status != null) {
            return Result.success(approvalMapper.findByStatus(status));
        }
        // status为null时返回全部
        return Result.success(approvalMapper.findByStatus(null));
    }

    /**
     * 管理者审批上链请求（通过时自动执行实际上链）
     */
    @PutMapping("/api/chain/approve/{id}")
    public Result<?> approveChain(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                  HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error(ResultVO.FORBIDDEN);
        }
        Long approverId = getAuthenticatedUserId(request);
        if (approverId == null) {
            return Result.error(ResultVO.TOKEN_EMPTY);
        }
        ChainApprovalRequest req = approvalMapper.findById(id);
        if (req == null) {
            return Result.error(new com.trace.model.vo.ResultVO(404002, "审批请求不存在", "审批请求不存在"));
        }
        if (req.getStatus() != 0) {
            return Result.error(new com.trace.model.vo.ResultVO(400002, "该请求已处理", "该请求已处理"));
        }

        Boolean approved = Boolean.valueOf(body.get("approved").toString());
        req.setStatus(approved ? 1 : 2);
        if (!approved) {
            req.setRejectReason(body.get("reason") != null ? body.get("reason").toString() : "");
        }
        req.setApproverId(approverId);
        approvalMapper.updateStatus(req);

        // 审批通过 → 执行实际上链
        String chainMsg = "";
        if (approved) {
            chainMsg = doActualOnChain(req.getProductId(), req.getProductNo(), req.getUserName());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("requestId", id);
        result.put("approved", approved);
        result.put("message", approved ? ("上链请求已通过" + (chainMsg.isEmpty() ? "" : "，" + chainMsg)) : "上链请求已拒绝");
        return Result.success(result);
    }

    /**
     * 执行实际上链：创建产品 + 上传已有业务记录
     */
    private String doActualOnChain(Long productId, String productNo, String username) {
        StringBuilder msg = new StringBuilder();
        try {
            // 1. 在链上创建产品
            Map<String, Object> createResult = bcosService.createProduct(ownerAddress, productNo);
            // 保存交易哈希到产品记录
            Object txHash = createResult != null ? createResult.get("transactionHash") : null;
            if (txHash != null) {
                Product product = productMapper.findById(productId);
                if (product != null) {
                    product.setTxHash(txHash.toString());
                    productMapper.update(product);
                }
            }
            msg.append("产品已上链");
        } catch (Exception e) {
            msg.append("产品上链失败:" + e.getMessage());
        }

        // 2. 上链已有的种植记录
        try {
            List<TracePlanting> plantings = plantingMapper.findByProductId(productId);
            for (TracePlanting p : plantings) {
                bcosService.recordPlanting(ownerAddress, p.getPlantingNo(), productNo, "hash_" + p.getId(), username);
            }
            if (!plantings.isEmpty()) msg.append("，种植记录已上链");
        } catch (Exception e) { /* 记录但不阻断 */ }

        // 3. 上链已有的加工记录
        try {
            List<TraceProcessing> processings = processingMapper.findByProductId(productId);
            for (TraceProcessing p : processings) {
                bcosService.recordProcessing(ownerAddress, p.getProcessingNo(), productNo, "hash_" + p.getId(), username);
            }
            if (!processings.isEmpty()) msg.append("，加工记录已上链");
        } catch (Exception e) { /* 记录但不阻断 */ }

        // 4. 上链已有的检测记录
        try {
            List<TraceTesting> testings = testingMapper.findByProductId(productId);
            for (TraceTesting t : testings) {
                bcosService.recordTesting(ownerAddress, t.getTestingNo(), productNo, "hash_" + t.getId(), username);
            }
            if (!testings.isEmpty()) msg.append("，检测记录已上链");
        } catch (Exception e) { /* 记录但不阻断 */ }

        // 5. 上链已有的物流记录
        try {
            List<TraceLogistics> logisticsList = logisticsMapper.findByProductId(productId);
            for (TraceLogistics l : logisticsList) {
                bcosService.recordLogistics(ownerAddress, l.getLogisticsNo(), productNo, "hash_" + l.getId(), username);
            }
            if (!logisticsList.isEmpty()) msg.append("，物流记录已上链");
        } catch (Exception e) { /* 记录但不阻断 */ }

        return msg.toString();
    }

    /**
     * 管理者手动补上链（用于已审批但未实际上链的产品）
     */
    @PostMapping("/api/chain/rechain")
    public Result<?> rechain(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.error(ResultVO.FORBIDDEN);
        }
        Long productId = Long.valueOf(body.get("productId").toString());
        Product product = productMapper.findById(productId);
        if (product == null) {
            return Result.error(new ResultVO(404001, "产品不存在", "产品不存在"));
        }
        String username = getAuthenticatedUsername(request);
        String chainMsg = doActualOnChain(productId, product.getProductNo(), username);
        Map<String, Object> result = new HashMap<>();
        result.put("productId", productId);
        result.put("productNo", product.getProductNo());
        result.put("message", "补上链完成：" + chainMsg);
        return Result.success(result);
    }

    private Long getAuthenticatedUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return userId instanceof Number ? ((Number) userId).longValue() : null;
    }

    private Integer getAuthenticatedRole(HttpServletRequest request) {
        Object role = request.getAttribute("role");
        return role instanceof Number ? ((Number) role).intValue() : null;
    }

    private String getAuthenticatedUsername(HttpServletRequest request) {
        Object username = request.getAttribute("username");
        return username instanceof String ? (String) username : null;
    }

    private boolean isAdmin(HttpServletRequest request) {
        Integer role = getAuthenticatedRole(request);
        return role != null && role == ROLE_ADMIN;
    }

    /**
     * 角色前置校验：检查用户是否有对应角色的操作记录
     */
    private String validateRoleRecords(Long productId, int userRole) {
        StringBuilder msg = new StringBuilder();
        switch (userRole) {
            case ROLE_FARMER:
                List<TracePlanting> plantings = plantingMapper.findByProductId(productId);
                if (plantings == null || plantings.isEmpty()) {
                    msg.append("[缺少种植记录] 农户必须先录入该产品的种植信息才能申请上链。");
                } else {
                    msg.append("[通过] 已有" + plantings.size() + "条种植记录。");
                }
                break;
            case ROLE_PROCESSOR:
                List<TraceProcessing> processings = processingMapper.findByProductId(productId);
                if (processings == null || processings.isEmpty()) {
                    msg.append("[缺少加工记录] 加工商必须先录入该产品的加工信息才能申请上链。");
                } else {
                    msg.append("[通过] 已有" + processings.size() + "条加工记录。");
                }
                break;
            case ROLE_TESTER:
                List<TraceTesting> testings = testingMapper.findByProductId(productId);
                if (testings == null || testings.isEmpty()) {
                    msg.append("[缺少检测记录] 检测机构必须先录入该产品的检测报告才能申请上链。");
                } else {
                    msg.append("[通过] 已有" + testings.size() + "条检测记录。");
                }
                break;
            case ROLE_LOGISTICS:
                List<TraceLogistics> logisticsList = logisticsMapper.findByProductId(productId);
                if (logisticsList == null || logisticsList.isEmpty()) {
                    msg.append("[缺少物流记录] 物流商必须先录入该产品的物流信息才能申请上链。");
                } else {
                    msg.append("[通过] 已有" + logisticsList.size() + "条物流记录。");
                }
                break;
            case ROLE_ADMIN:
                msg.append("[管理员] 监管者无需前置校验。");
                break;
            default:
                msg.append("[未知角色] 无法校验。");
                break;
        }
        return msg.toString();
    }
}
