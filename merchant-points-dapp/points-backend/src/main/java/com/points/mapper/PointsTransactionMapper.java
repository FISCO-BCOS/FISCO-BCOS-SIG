package com.points.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.points.entity.PointsTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface PointsTransactionMapper extends BaseMapper<PointsTransaction> {

    @Select("SELECT COALESCE(SUM(amount), 0) FROM points_transaction WHERE tx_type = 1 AND DATE(create_time) = CURDATE()")
    BigDecimal sumTodayIssued();

    @Select("SELECT COALESCE(SUM(amount), 0) FROM points_transaction WHERE tx_type = 4 AND DATE(create_time) = CURDATE()")
    BigDecimal sumTodayConsumed();

    @Select("SELECT COUNT(*) FROM points_transaction WHERE DATE(create_time) = CURDATE()")
    int countTodayTransactions();
}
