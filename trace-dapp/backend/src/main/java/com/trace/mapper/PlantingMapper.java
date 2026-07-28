package com.trace.mapper;

import com.trace.model.TracePlanting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PlantingMapper {
    List<TracePlanting> findAll();
    TracePlanting findById(@Param("id") Long id);
    List<TracePlanting> findByProductId(@Param("productId") Long productId);
    int insert(TracePlanting planting);
}