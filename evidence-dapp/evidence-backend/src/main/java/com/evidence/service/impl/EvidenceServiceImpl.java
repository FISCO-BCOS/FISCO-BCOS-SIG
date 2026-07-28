package com.evidence.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evidence.blockchain.BlockchainEvidenceService;
import com.evidence.blockchain.ChainResult;
import com.evidence.common.PageResult;
import com.evidence.common.R;
import com.evidence.common.ResultCode;
import com.evidence.mapper.EvidenceRecordMapper;
import com.evidence.mapper.SysUserMapper;
import com.evidence.mapper.VerifyRecordMapper;
import com.evidence.mapper.WorkFileMapper;
import com.evidence.model.bo.SaveEvidenceRequest;
import com.evidence.model.entity.EvidenceRecordEntity;
import com.evidence.model.entity.SysUserEntity;
import com.evidence.model.entity.VerifyRecordEntity;
import com.evidence.model.entity.WorkFileEntity;
import com.evidence.model.enums.EvidenceStatusEnum;
import com.evidence.model.vo.EvidenceDetailVO;
import com.evidence.model.vo.EvidenceVO;
import com.evidence.model.vo.UserVO;
import com.evidence.model.vo.VerifyVO;
import com.evidence.service.EvidenceService;
import com.evidence.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class EvidenceServiceImpl implements EvidenceService {

    @Autowired
    private EvidenceRecordMapper evidenceRecordMapper;

    @Autowired
    private VerifyRecordMapper verifyRecordMapper;

    @Autowired
    private WorkFileMapper workFileMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private BlockchainEvidenceService blockchainEvidenceService;

    @Value("${fisco.bcos.contract.evidence-address}")
    private String contractAddress;

    @PostConstruct
    public void fixHistoricalVerifierNames() {
        try {
            LambdaQueryWrapper<VerifyRecordEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(VerifyRecordEntity::getVerifierName, "匿名用户");
            wrapper.isNotNull(VerifyRecordEntity::getVerifierId);
            List<VerifyRecordEntity> records = verifyRecordMapper.selectList(wrapper);
            int fixed = 0;
            for (VerifyRecordEntity vr : records) {
                SysUserEntity user = sysUserMapper.selectById(vr.getVerifierId());
                if (user != null) {
                    String name = user.getRealName();
                    if (name == null || name.trim().isEmpty()) {
                        name = user.getUsername();
                    }
                    vr.setVerifierName(name);
                    verifyRecordMapper.updateById(vr);
                    fixed++;
                }
            }
            if (fixed > 0) {
                System.out.println("[Fix] 已修复 " + fixed + " 条历史验证记录的用户名");
            }
        } catch (Exception e) {
            System.err.println("[Fix] 修复历史验证记录用户名失败: " + e.getMessage());
        }
    }

    @Override
    public R<EvidenceDetailVO> saveEvidence(Long userId, MultipartFile[] files, SaveEvidenceRequest request) {
        String evidenceNo = "EV" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));

        StringBuilder hashBuilder = new StringBuilder();
        for (MultipartFile file : files) {
            try {
                hashBuilder.append(DigestUtil.sha256Hex(file.getBytes()));
            } catch (Exception e) {
                return R.fail(ResultCode.INTERNAL_ERROR.getCode(), "文件读取失败");
            }
        }
        String workHash = DigestUtil.sha256Hex(hashBuilder.toString());

        LambdaQueryWrapper<EvidenceRecordEntity> hashWrapper = new LambdaQueryWrapper<>();
        hashWrapper.eq(EvidenceRecordEntity::getWorkHash, workHash);
        if (evidenceRecordMapper.selectCount(hashWrapper) > 0) {
            return R.fail(ResultCode.CONFLICT.getCode(), "存证哈希已存在");
        }

        EvidenceRecordEntity record = new EvidenceRecordEntity();
        record.setUserId(userId);
        record.setEvidenceNo(evidenceNo);
        record.setWorkTitle(request.getWorkTitle());
        record.setWorkDesc(request.getWorkDesc());
        record.setWorkCategory(request.getWorkCategory());
        record.setWorkHash(workHash);
        record.setStatus(EvidenceStatusEnum.PENDING.getCode());
        record.setVerifyCount(0);
        record.setEvidenceTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        evidenceRecordMapper.insert(record);

        for (MultipartFile file : files) {
            try {
                WorkFileEntity fileEntity = fileUploadService.uploadFile(file, userId, record.getId());
                workFileMapper.insert(fileEntity);
            } catch (Exception e) {
                return R.fail(ResultCode.INTERNAL_ERROR.getCode(), "文件保存失败");
            }
        }

        if (Boolean.TRUE.equals(request.getUploadToChain())) {
            try {
                String metadata = JSONUtil.createObj()
                        .set("evidenceNo", evidenceNo)
                        .set("workTitle", request.getWorkTitle())
                        .set("workCategory", request.getWorkCategory())
                        .toString();
                ChainResult chainResult = blockchainEvidenceService.saveEvidenceOnChain(workHash, metadata);
                if (chainResult.isSuccess()) {
                    record.setStatus(EvidenceStatusEnum.ON_CHAIN.getCode());
                    record.setTxHash(chainResult.getTxHash());
                    record.setBlockNumber(chainResult.getBlockNumber());
                    record.setContractAddress(contractAddress);
                } else {
                    record.setStatus(EvidenceStatusEnum.FAILED.getCode());
                }
            } catch (Exception e) {
                record.setStatus(EvidenceStatusEnum.FAILED.getCode());
            }
            record.setUpdateTime(LocalDateTime.now());
            evidenceRecordMapper.updateById(record);
        }

        return getEvidenceDetail(record.getId());
    }

    @Override
    public R<EvidenceDetailVO> retryChain(Long evidenceId, Long userId) {
        EvidenceRecordEntity record = evidenceRecordMapper.selectById(evidenceId);
        if (record == null) {
            return R.fail(ResultCode.NOT_FOUND.getCode(), "存证记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            return R.fail(ResultCode.FORBIDDEN.getCode(), "只有存证本人可以执行上链操作");
        }
        if (record.getStatus() != null && record.getStatus() == 1) {
            return R.fail(ResultCode.CONFLICT.getCode(), "该存证已上链，无需重复操作");
        }

        try {
            String metadata = JSONUtil.createObj()
                    .set("evidenceNo", record.getEvidenceNo())
                    .set("workTitle", record.getWorkTitle())
                    .set("workCategory", record.getWorkCategory())
                    .toString();
            ChainResult chainResult = blockchainEvidenceService.saveEvidenceOnChain(record.getWorkHash(), metadata);
            if (chainResult.isSuccess()) {
                record.setStatus(EvidenceStatusEnum.ON_CHAIN.getCode());
                record.setTxHash(chainResult.getTxHash());
                record.setBlockNumber(chainResult.getBlockNumber());
                record.setContractAddress(contractAddress);
            } else {
                record.setStatus(EvidenceStatusEnum.FAILED.getCode());
            }
        } catch (Exception e) {
            record.setStatus(EvidenceStatusEnum.FAILED.getCode());
        }
        record.setUpdateTime(LocalDateTime.now());
        evidenceRecordMapper.updateById(record);

        return getEvidenceDetail(record.getId());
    }

    @Override
    public R<PageResult<EvidenceVO>> listEvidences(Integer pageNum, Integer pageSize, String keyword, String category, Integer status) {
        IPage<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        IPage<Map<String, Object>> result = evidenceRecordMapper.selectEvidencePage(page, keyword, category, status);
        List<EvidenceVO> voList = result.getRecords().stream()
                .map(this::mapToEvidenceVO)
                .collect(Collectors.toList());
        return R.ok(new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), voList));
    }

    @Override
    public R<EvidenceDetailVO> getEvidenceDetail(Long id) {
        EvidenceRecordEntity record = evidenceRecordMapper.selectById(id);
        if (record == null) {
            return R.fail(ResultCode.NOT_FOUND.getCode(), "存证记录不存在");
        }

        EvidenceDetailVO detail = new EvidenceDetailVO();
        detail.setId(record.getId());
        detail.setEvidenceNo(record.getEvidenceNo());
        detail.setWorkTitle(record.getWorkTitle());
        detail.setWorkDesc(record.getWorkDesc());
        detail.setWorkCategory(record.getWorkCategory());
        detail.setWorkHash(record.getWorkHash());
        detail.setTxHash(record.getTxHash());
        detail.setBlockNumber(record.getBlockNumber());
        detail.setContractAddress(record.getContractAddress());
        detail.setEvidenceTime(record.getEvidenceTime());
        detail.setVerifyCount(record.getVerifyCount());
        detail.setStatus(record.getStatus());
        detail.setCreateTime(record.getCreateTime());

        SysUserEntity user = sysUserMapper.selectById(record.getUserId());
        if (user != null) {
            UserVO userVO = new UserVO();
            BeanUtil.copyProperties(user, userVO);
            detail.setUser(userVO);
        }

        LambdaQueryWrapper<WorkFileEntity> fileWrapper = new LambdaQueryWrapper<>();
        fileWrapper.eq(WorkFileEntity::getEvidenceId, id);
        List<WorkFileEntity> fileEntities = workFileMapper.selectList(fileWrapper);
        List<EvidenceDetailVO.WorkFileVO> fileVOs = fileEntities.stream().map(f -> {
            EvidenceDetailVO.WorkFileVO fileVO = new EvidenceDetailVO.WorkFileVO();
            fileVO.setId(f.getId());
            fileVO.setFileName(f.getFileName());
            fileVO.setFilePath(f.getFilePath());
            fileVO.setFileSize(f.getFileSize());
            fileVO.setFileType(f.getFileType());
            fileVO.setFileHash(f.getFileHash());
            fileVO.setUploadTime(f.getUploadTime());
            return fileVO;
        }).collect(Collectors.toList());
        detail.setFiles(fileVOs);

        LambdaQueryWrapper<VerifyRecordEntity> vrWrapper = new LambdaQueryWrapper<>();
        vrWrapper.eq(VerifyRecordEntity::getEvidenceId, id);
        vrWrapper.orderByDesc(VerifyRecordEntity::getVerifyTime);
        List<VerifyRecordEntity> verifyEntities = verifyRecordMapper.selectList(vrWrapper);
        List<EvidenceDetailVO.VerifyRecordVO> verifyRecords = verifyEntities.stream().map(v -> {
            EvidenceDetailVO.VerifyRecordVO vo = new EvidenceDetailVO.VerifyRecordVO();
            vo.setId(v.getId());
            vo.setVerifierName(v.getVerifierName());
            vo.setIsExist(v.getIsExist());
            vo.setMatchResult(v.getMatchResult());
            vo.setVerifyTime(v.getVerifyTime());
            return vo;
        }).collect(Collectors.toList());
        detail.setVerifyRecords(verifyRecords);

        return R.ok(detail);
    }

    @Override
    public R<VerifyVO> verifyEvidence(String hash, Long verifierId) {
        if (hash == null || !hash.matches("^[a-fA-F0-9]{64}$")) {
            return R.fail(ResultCode.PARAM_ERROR.getCode(), "哈希格式不正确，需为64位十六进制");
        }

        LambdaQueryWrapper<EvidenceRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EvidenceRecordEntity::getWorkHash, hash);
        EvidenceRecordEntity record = evidenceRecordMapper.selectOne(wrapper);

        VerifyVO verifyVO = new VerifyVO();
        verifyVO.setVerifyTime(LocalDateTime.now());

        if (record == null) {
            verifyVO.setIsExist(false);
            verifyVO.setMatchResult("链上未找到该哈希对应的存证记录");
            return R.ok(verifyVO);
        }

        try {
            ChainResult chainResult = blockchainEvidenceService.verifyEvidenceExists(hash);
            if (chainResult.isSuccess()) {
                verifyVO.setIsExist(true);
                verifyVO.setMatchResult("链上验证通过，存证真实有效");
            } else if (record.getStatus() != null && record.getStatus() == 1 && record.getTxHash() != null && !record.getTxHash().isEmpty()) {
                verifyVO.setIsExist(true);
                verifyVO.setMatchResult("存证已上链确认（链上核验暂时不可达，基于本地记录验证通过）");
            } else {
                verifyVO.setIsExist(false);
                verifyVO.setMatchResult("链上验证失败");
            }
        } catch (Exception e) {
            if (record.getStatus() != null && record.getStatus() == 1 && record.getTxHash() != null && !record.getTxHash().isEmpty()) {
                verifyVO.setIsExist(true);
                verifyVO.setMatchResult("存证已上链确认（区块链服务暂时不可用，基于本地记录验证通过）");
            } else {
                verifyVO.setIsExist(false);
                verifyVO.setMatchResult("区块链服务异常，验证失败");
            }
        }

        record.setVerifyCount(record.getVerifyCount() + 1);
        record.setUpdateTime(LocalDateTime.now());
        evidenceRecordMapper.updateById(record);

        VerifyRecordEntity vr = new VerifyRecordEntity();
        vr.setEvidenceId(record.getId());
        if (verifierId != null) {
            vr.setVerifierId(verifierId);
            SysUserEntity verifier = sysUserMapper.selectById(verifierId);
            if (verifier != null) {
                String name = verifier.getRealName();
                if (name == null || name.trim().isEmpty()) {
                    name = verifier.getUsername();
                }
                vr.setVerifierName(name);
            } else {
                vr.setVerifierName("未知用户");
            }
        } else {
            vr.setVerifierName("匿名用户");
        }
        vr.setHash(hash);
        vr.setIsExist(verifyVO.getIsExist());
        vr.setMatchResult(verifyVO.getMatchResult());
        vr.setVerifyTime(LocalDateTime.now());
        verifyRecordMapper.insert(vr);

        EvidenceVO evidenceVO = mapRecordToEvidenceVO(record);
        verifyVO.setEvidenceInfo(evidenceVO);

        return R.ok(verifyVO);
    }

    @Override
    public R<PageResult<EvidenceVO>> myEvidences(Long userId, Integer pageNum, Integer pageSize, String keyword, String category, Integer status) {
        IPage<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        IPage<Map<String, Object>> result = evidenceRecordMapper.selectMyEvidencePage(page, keyword, category, status, userId);
        List<EvidenceVO> voList = result.getRecords().stream()
                .map(this::mapToEvidenceVO)
                .collect(Collectors.toList());
        return R.ok(new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), voList));
    }

    private EvidenceVO mapToEvidenceVO(Map<String, Object> map) {
        EvidenceVO vo = new EvidenceVO();
        vo.setId(toLong(map.get("id")));
        vo.setEvidenceNo((String) map.get("evidence_no"));
        vo.setWorkTitle((String) map.get("work_title"));
        vo.setWorkDesc((String) map.get("work_desc"));
        vo.setWorkCategory((String) map.get("work_category"));
        vo.setWorkHash((String) map.get("work_hash"));
        vo.setTxHash((String) map.get("tx_hash"));
        vo.setBlockNumber(toLong(map.get("block_number")));
        vo.setEvidenceTime(toLocalDateTime(map.get("evidence_time")));
        vo.setVerifyCount(toInteger(map.get("verify_count")));
        vo.setStatus(toInteger(map.get("status")));
        vo.setUserName((String) map.get("user_name"));
        vo.setFileCount(toInteger(map.get("file_count")));
        return vo;
    }

    private EvidenceVO mapRecordToEvidenceVO(EvidenceRecordEntity record) {
        EvidenceVO vo = new EvidenceVO();
        vo.setId(record.getId());
        vo.setEvidenceNo(record.getEvidenceNo());
        vo.setWorkTitle(record.getWorkTitle());
        vo.setWorkDesc(record.getWorkDesc());
        vo.setWorkCategory(record.getWorkCategory());
        vo.setWorkHash(record.getWorkHash());
        vo.setTxHash(record.getTxHash());
        vo.setBlockNumber(record.getBlockNumber());
        vo.setEvidenceTime(record.getEvidenceTime());
        vo.setVerifyCount(record.getVerifyCount());
        vo.setStatus(record.getStatus());
        return vo;
    }

    private Long toLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) return ((Number) obj).longValue();
        return Long.valueOf(obj.toString());
    }

    private Integer toInteger(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) return ((Number) obj).intValue();
        return Integer.valueOf(obj.toString());
    }

    private LocalDateTime toLocalDateTime(Object obj) {
        if (obj == null) return null;
        if (obj instanceof LocalDateTime) return (LocalDateTime) obj;
        if (obj instanceof Timestamp) return ((Timestamp) obj).toLocalDateTime();
        return LocalDateTime.parse(obj.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
