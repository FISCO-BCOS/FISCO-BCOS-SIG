package com.evidence.service;

import com.evidence.common.R;
import com.evidence.common.PageResult;
import com.evidence.model.bo.SaveEvidenceRequest;
import com.evidence.model.vo.EvidenceDetailVO;
import com.evidence.model.vo.EvidenceVO;
import com.evidence.model.vo.VerifyVO;
import org.springframework.web.multipart.MultipartFile;

public interface EvidenceService {

    R<EvidenceDetailVO> saveEvidence(Long userId, MultipartFile[] files, SaveEvidenceRequest request);

    R<EvidenceDetailVO> retryChain(Long evidenceId, Long userId);

    R<PageResult<EvidenceVO>> listEvidences(Integer pageNum, Integer pageSize, String keyword, String category, Integer status);

    R<EvidenceDetailVO> getEvidenceDetail(Long id);

    R<VerifyVO> verifyEvidence(String hash, Long userId);

    R<PageResult<EvidenceVO>> myEvidences(Long userId, Integer pageNum, Integer pageSize, String keyword, String category, Integer status);
}
