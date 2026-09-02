package com.points.controller;

import com.points.dto.request.LoginRequest;
import com.points.dto.request.RegisterRequest;
import com.points.dto.response.LoginResponse;
import com.points.dto.response.R;
import com.points.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "认证管理")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @ApiOperation("商户注册")
    @PostMapping("/register")
    public R<String> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }
}
