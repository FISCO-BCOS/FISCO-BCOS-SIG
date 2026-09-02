package com.evidence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evidence.model.entity.EvidenceRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface EvidenceRecordMapper extends BaseMapper<EvidenceRecordEntity> {
    IPage<Map<String, Object>> selectEvidencePage(IPage<?> page, @Param("keyword") String keyword, @Param("category") String category, @Param("status") Integer status);

    IPage<Map<String, Object>> selectMyEvidencePage(IPage<?> page, @Param("keyword") String keyword, @Param("category") String category, @Param("status") Integer status, @Param("userId") Long userId);
}
