package com.trace.mapper;

import com.trace.model.TraceScanRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ScanRecordMapper {
    List<TraceScanRecord> findAll();
    List<TraceScanRecord> findByProductId(@Param("productId") Long productId);
    int insert(TraceScanRecord scanRecord);
    int countByProductId(@Param("productId") Long productId);
}