package com.trace.interceptor;

import com.trace.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            writeUnauthorized(response);
            return false;
        }
        try {
            token = token.substring(7);
            Claims claims = jwtUtil.parseToken(token);
            request.setAttribute("username", claims.getSubject());
            request.setAttribute("role", ((Number) claims.get("role")).intValue());
            Object userId = claims.get("userId");
            if (userId instanceof Number) {
                request.setAttribute("userId", ((Number) userId).longValue());
            }
            return true;
        } catch (Exception e) {
            writeUnauthorized(response);
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write("{\"code\":500003,\"msg\":\"Token已失效，请重新登录\",\"data\":null}");
    }
}
