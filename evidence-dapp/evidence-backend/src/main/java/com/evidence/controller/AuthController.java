package com.evidence.controller;

import com.evidence.common.R;
import com.evidence.model.bo.LoginRequest;
import com.evidence.model.bo.RegisterRequest;
import com.evidence.model.vo.LoginVO;
import com.evidence.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public R<Long> register(@RequestBody @Validated RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public R<LoginVO> login(@RequestBody @Validated LoginRequest request, HttpServletResponse response) {
        return authService.login(request, response);
    }

    @GetMapping("/refresh")
    public R<LoginVO> refresh(@CookieValue(value = "refresh_token", required = false) String refreshToken, HttpServletResponse response) {
        return authService.refreshToken(refreshToken, response);
    }
}
