package com.evidence.controller;

import com.evidence.common.R;
import com.evidence.common.PageResult;
import com.evidence.model.bo.SaveEvidenceRequest;
import com.evidence.model.vo.EvidenceDetailVO;
import com.evidence.model.vo.EvidenceVO;
import com.evidence.model.vo.VerifyVO;
import com.evidence.service.EvidenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/evidence")
public class EvidenceController {

    @Autowired
    EvidenceService evidenceService;

    @PostMapping("/save")
    public R<EvidenceDetailVO> saveEvidence(
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @Validated SaveEvidenceRequest request,
            HttpServletRequest httpRequest) {
        if (files == null || files.length == 0) {
            return R.fail(400, "请上传至少一个文件");
        }
        Long userId = (Long) httpRequest.getAttribute("userId");
        return evidenceService.saveEvidence(userId, files, request);
    }

    @GetMapping("/list")
    public R<PageResult<EvidenceVO>> listEvidences(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {
        return evidenceService.listEvidences(pageNum, pageSize, keyword, category, status);
    }

    @GetMapping("/{id}")
    public R<EvidenceDetailVO> getEvidenceDetail(@PathVariable Long id) {
        return evidenceService.getEvidenceDetail(id);
    }

    @GetMapping("/verify/{hash}")
    public R<VerifyVO> verifyEvidence(@PathVariable String hash, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return evidenceService.verifyEvidence(hash, userId);
    }

    @GetMapping("/my")
    public R<PageResult<EvidenceVO>> myEvidences(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return evidenceService.myEvidences(userId, pageNum, pageSize, keyword, category, status);
    }

    @PostMapping("/{id}/chain")
    public R<EvidenceDetailVO> retryChain(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return evidenceService.retryChain(id, userId);
    }
}