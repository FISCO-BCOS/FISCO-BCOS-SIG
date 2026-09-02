package com.trace.config;

import com.trace.interceptor.JwtInterceptor;
import com.trace.interceptor.LoginRateLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private LoginRateLimitInterceptor loginRateLimitInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./static/uploads/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录速率限制（防暴力破解）
        registry.addInterceptor(loginRateLimitInterceptor)
                .addPathPatterns("/api/auth/login");

        // JWT认证拦截器
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/auth/**", "/api/trace/query/**");
    }

    /**
     * CORS跨域配置：显式指定允许的来源，禁止使用通配符*
     * 原则6：CORS必须显式配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("http://localhost:8022", "http://127.0.0.1:8022")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "X-Requested-With")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
