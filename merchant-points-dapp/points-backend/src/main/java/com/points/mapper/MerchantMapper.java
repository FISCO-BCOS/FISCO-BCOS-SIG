package com.points.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.points.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {

    @Select("SELECT business_type as businessType, COUNT(*) as count FROM merchant WHERE audit_status = 1 GROUP BY business_type")
    List<Map<String, Object>> countByBusinessType();

    @Select("SELECT COUNT(*) FROM merchant WHERE audit_status = 0")
    int countPendingAudit();

    @Select("SELECT COUNT(*) FROM merchant WHERE audit_status = 1")
    int countActive();

    @Select("SELECT SUM(total_issued) FROM merchant WHERE audit_status = 1")
    Double sumTotalIssued();

    @Select("SELECT SUM(total_consumed) FROM merchant WHERE audit_status = 1")
    Double sumTotalConsumed();
}
