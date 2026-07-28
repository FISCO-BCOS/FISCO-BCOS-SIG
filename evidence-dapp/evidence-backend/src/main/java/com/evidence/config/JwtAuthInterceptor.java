package com.evidence.config;

import com.evidence.common.R;
import com.evidence.common.ResultCode;
import com.evidence.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorizedResponse(response, "未提供认证令牌");
            return false;
        }

        String token = authHeader.substring(7);
        try {
            if (!jwtUtil.isAccessToken(token)) {
                writeUnauthorizedResponse(response, "令牌类型无效");
                return false;
            }
            Long userId = jwtUtil.parseToken(token).getClaim("userId").asLong();
            request.setAttribute("userId", userId);
            return true;
        } catch (Exception e) {
            writeUnauthorizedResponse(response, "认证令牌无效或已过期");
            return false;
        }
    }

    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        R<Void> result = R.fail(ResultCode.UNAUTHORIZED.getCode(), message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
