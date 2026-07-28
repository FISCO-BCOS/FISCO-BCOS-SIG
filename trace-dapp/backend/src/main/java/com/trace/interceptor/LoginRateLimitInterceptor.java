package com.trace.interceptor;

import com.trace.model.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录接口速率限制拦截器
 * 防止暴力破解：同一IP在时间窗口内最多允许N次登录尝试
 */
@Component
public class LoginRateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LoginRateLimitInterceptor.class);

    /** 时间窗口：60秒 */
    private static final long WINDOW_MS = 60_000L;
    /** 窗口内最大尝试次数 */
    private static final int MAX_ATTEMPTS = 5;
    /** 超限锁定时间：5分钟 */
    private static final long LOCK_TIME_MS = 300_000L;

    // IP -> { 尝试次数, 窗口开始时间, 是否被锁定, 锁定开始时间 }
    private final Map<String, RateLimitEntry> ipRecords = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        if (!"/api/auth/login".equals(uri)) {
            return true;
        }

        String clientIp = getClientIp(request);
        RateLimitEntry entry = ipRecords.computeIfAbsent(clientIp, k -> new RateLimitEntry());

        long now = System.currentTimeMillis();

        // 检查是否在锁定期内
        if (entry.locked && (now - entry.lockTime < LOCK_TIME_MS)) {
            long remainingSec = (LOCK_TIME_MS - (now - entry.lockTime)) / 1000;
            writeResponse(response, 429, "登录尝试过于频繁，请 " + remainingSec + " 秒后重试");
            log.warn("IP {} 登录被限流，剩余 {} 秒", clientIp, remainingSec);
            return false;
        }

        // 检查是否超出窗口限制
        if (now - entry.windowStart > WINDOW_MS) {
            // 窗口重置
            entry.attempts = 1;
            entry.windowStart = now;
            entry.locked = false;
        } else {
            entry.attempts++;
            if (entry.attempts > MAX_ATTEMPTS) {
                entry.locked = true;
                entry.lockTime = now;
                long lockSec = LOCK_TIME_MS / 1000;
                writeResponse(response, 429, "登录失败次数过多，已临时锁定 " + lockSec + " 秒");
                log.warn("IP {} 超过登录限制({}次/{}s)，已锁定", clientIp, MAX_ATTEMPTS, WINDOW_MS / 1000);
                return false;
            }
        }

        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 取第一个IP（多级代理情况）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private void writeResponse(HttpServletResponse response, int code, String msg) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(new Result<>(code, msg, null)));
    }

    static class RateLimitEntry {
        int attempts = 0;
        long windowStart = System.currentTimeMillis();
        boolean locked = false;
        long lockTime = 0;
    }
}
