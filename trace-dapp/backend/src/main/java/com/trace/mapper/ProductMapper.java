package com.trace.mapper;

import com.trace.model.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProductMapper {
    List<Product> findAll(@Param("offset") int offset, @Param("limit") int limit);
    Product findById(@Param("id") Long id);
    Product findByProductNo(@Param("productNo") String productNo);
    int insert(Product product);
    int update(Product product);
    int countByDate(@Param("datePrefix") String datePrefix);
    int countByCondition(@Param("keyword") String keyword, @Param("category") String category, @Param("status") Integer status, @Param("userId") Long userId);
    List<Product> findByCondition(@Param("keyword") String keyword, @Param("category") String category, @Param("status") Integer status, @Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit, @Param("sortBy") String sortBy, @Param("sortOrder") String sortOrder);
    int countTotal();
    int countTodayNew(@Param("todayStart") String todayStart);
    int countPendingRecords();
    int countTotalScans();
    List<Product> findLatest(@Param("limit") int limit);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    int updateNullFarmerId(@Param("farmerId") Long farmerId);
}