package com.trace.service;

import com.trace.model.Result;
import com.trace.model.bo.ProductCreateDTO;
import com.trace.model.vo.CreateProductResultVO;
import com.trace.model.vo.DashboardVO;
import com.trace.model.vo.ProductDetailVO;
import java.util.Map;

public interface IProductService {
    Result<CreateProductResultVO> createProduct(ProductCreateDTO dto, String operatorUsername);
    Result<Map<String,Object>> listProducts(int pageNum, int pageSize, String keyword, String category, Integer status, Long userId, String sortBy, String sortOrder);
    Result<ProductDetailVO> getProductDetail(Long id);
    Result<String> updateProduct(Long id, ProductCreateDTO dto);
    Result<DashboardVO> getDashboard();
    Result<String> updateStatus(Long id, String status);
}