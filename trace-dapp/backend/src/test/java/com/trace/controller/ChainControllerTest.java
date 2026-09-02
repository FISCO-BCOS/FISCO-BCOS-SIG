package com.trace.controller;

import com.trace.mapper.ChainApprovalMapper;
import com.trace.mapper.PlantingMapper;
import com.trace.mapper.ProductMapper;
import com.trace.model.ChainApprovalRequest;
import com.trace.model.Product;
import com.trace.model.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChainControllerTest {

    private ChainController controller;
    private ChainApprovalMapper approvalMapper;
    private ProductMapper productMapper;

    @BeforeEach
    void setUp() {
        controller = new ChainController();
        approvalMapper = mock(ChainApprovalMapper.class);
        productMapper = mock(ProductMapper.class);
        ReflectionTestUtils.setField(controller, "approvalMapper", approvalMapper);
        ReflectionTestUtils.setField(controller, "productMapper", productMapper);
        ReflectionTestUtils.setField(controller, "plantingMapper", mock(PlantingMapper.class));
    }

    @Test
    void submitChainUsesAuthenticatedUserClaims() {
        Product product = new Product();
        product.setId(7L);
        product.setProductNo("P-007");
        when(productMapper.findById(7L)).thenReturn(product);
        when(approvalMapper.findByProductId(7L)).thenReturn(Collections.emptyList());

        MockHttpServletRequest request = authenticatedRequest(42L, 1, "farmer");
        Map<String, Object> body = new HashMap<>();
        body.put("productId", 7L);

        Result<?> result = controller.submitChain(body, request);

        assertEquals(200, result.getCode());
        ArgumentCaptor<ChainApprovalRequest> captor = ArgumentCaptor.forClass(ChainApprovalRequest.class);
        verify(approvalMapper).insert(captor.capture());
        assertEquals(42L, captor.getValue().getUserId());
        assertEquals(1, captor.getValue().getUserRole());
        assertEquals("farmer", captor.getValue().getUserName());
    }

    @Test
    void approveChainRejectsNonAdminUser() {
        MockHttpServletRequest request = authenticatedRequest(42L, 1, "farmer");
        Map<String, Object> body = Collections.<String, Object>singletonMap("approved", false);

        Result<?> result = controller.approveChain(9L, body, request);

        assertEquals(403001, result.getCode());
        verify(approvalMapper, never()).findById(any());
    }

    @Test
    void approveChainStoresAuthenticatedAdminId() {
        ChainApprovalRequest approval = new ChainApprovalRequest();
        approval.setId(9L);
        approval.setStatus(0);
        when(approvalMapper.findById(9L)).thenReturn(approval);
        MockHttpServletRequest request = authenticatedRequest(99L, 0, "admin");
        Map<String, Object> body = Collections.<String, Object>singletonMap("approved", false);

        Result<?> result = controller.approveChain(9L, body, request);

        assertEquals(200, result.getCode());
        assertEquals(99L, approval.getApproverId());
        verify(approvalMapper).updateStatus(approval);
    }

    private MockHttpServletRequest authenticatedRequest(Long userId, Integer role, String username) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", userId);
        request.setAttribute("role", role);
        request.setAttribute("username", username);
        return request;
    }
}
