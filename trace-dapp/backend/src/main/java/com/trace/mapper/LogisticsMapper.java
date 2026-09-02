package com.trace.mapper;

import com.trace.model.TraceLogistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface LogisticsMapper {
    List<TraceLogistics> findAll();
    TraceLogistics findById(@Param("id") Long id);
    List<TraceLogistics> findByProductId(@Param("productId") Long productId);
    int insert(TraceLogistics logistics);
}