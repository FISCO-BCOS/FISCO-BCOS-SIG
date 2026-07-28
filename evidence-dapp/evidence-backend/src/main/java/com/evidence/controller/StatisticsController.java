package com.evidence.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evidence.common.R;
import com.evidence.mapper.EvidenceRecordMapper;
import com.evidence.model.entity.EvidenceRecordEntity;
import com.evidence.model.vo.StatisticsVO;

import org.fisco.bcos.sdk.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private EvidenceRecordMapper evidenceRecordMapper;

    @Autowired
    private Client fiscoClient;

    @GetMapping("/summary")
    public R<StatisticsVO> summary() {
        StatisticsVO vo = new StatisticsVO();

        QueryWrapper<EvidenceRecordEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        vo.setTotalEvidences(evidenceRecordMapper.selectCount(wrapper));

        QueryWrapper<EvidenceRecordEntity> monthWrapper = new QueryWrapper<>();
        monthWrapper.eq("status", 1);
        monthWrapper.ge("evidence_time", LocalDate.now().withDayOfMonth(1).atStartOfDay());
        vo.setMonthlyNew(evidenceRecordMapper.selectCount(monthWrapper));

        Long totalVerify = evidenceRecordMapper.selectList(null).stream()
                .mapToLong(r -> r.getVerifyCount() != null ? r.getVerifyCount() : 0L).sum();
        vo.setTotalVerifications(totalVerify);

        try {
            long blockNum = fiscoClient.getBlockNumber().getBlockNumber().longValue();
            vo.setLatestBlockNumber(blockNum > 0 ? blockNum : 0L);
        } catch (Exception e) {
            QueryWrapper<EvidenceRecordEntity> blockWrapper = new QueryWrapper<>();
            blockWrapper.eq("status", 1).isNotNull("block_number").gt("block_number", 1000)
                    .orderByDesc("block_number").last("LIMIT 1");
            EvidenceRecordEntity latest = evidenceRecordMapper.selectOne(blockWrapper);
            vo.setLatestBlockNumber(latest != null && latest.getBlockNumber() != null && latest.getBlockNumber() > 1000
                    ? latest.getBlockNumber() : 0L);
        }

        return R.ok(vo);
    }
}
