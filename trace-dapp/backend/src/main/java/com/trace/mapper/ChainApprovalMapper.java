package com.trace.mapper;

import com.trace.model.ChainApprovalRequest;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ChainApprovalMapper {

    int insert(ChainApprovalRequest request);

    ChainApprovalRequest findById(Long id);

    List<ChainApprovalRequest> findByStatus(Integer status);

    List<ChainApprovalRequest> findByProductId(Long productId);

    int updateStatus(ChainApprovalRequest request);
}
