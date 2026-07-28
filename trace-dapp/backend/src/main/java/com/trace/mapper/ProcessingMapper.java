package com.trace.mapper;

import com.trace.model.TraceProcessing;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProcessingMapper {
    List<TraceProcessing> findAll();
    TraceProcessing findById(@Param("id") Long id);
    List<TraceProcessing> findByProductId(@Param("productId") Long productId);
    int insert(TraceProcessing processing);
}