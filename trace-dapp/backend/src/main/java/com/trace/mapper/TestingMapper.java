package com.trace.mapper;

import com.trace.model.TraceTesting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface TestingMapper {
    List<TraceTesting> findAll();
    TraceTesting findById(@Param("id") Long id);
    List<TraceTesting> findByProductId(@Param("productId") Long productId);
    int insert(TraceTesting testing);
}