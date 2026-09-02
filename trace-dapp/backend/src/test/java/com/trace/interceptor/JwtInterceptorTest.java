package com.trace.interceptor;

import com.trace.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtInterceptorTest {

    private JwtInterceptor interceptor;
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "test-secret-that-is-long-enough");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600L);
        interceptor = new JwtInterceptor();
        ReflectionTestUtils.setField(interceptor, "jwtUtil", jwtUtil);
    }

    @Test
    void rejectsInvalidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/chain/records");
        request.addHeader("Authorization", "Bearer definitely-invalid");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(401, response.getStatus());
    }

    @Test
    void acceptsValidTokenAndPublishesClaims() throws Exception {
        String token = jwtUtil.generateToken("farmer", 1, 42L);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/chain/records");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertEquals("farmer", request.getAttribute("username"));
        assertEquals(1, request.getAttribute("role"));
        assertEquals(42L, request.getAttribute("userId"));
    }
}
