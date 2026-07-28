package com.trace.controller;

import com.trace.model.Result;
import com.trace.model.bo.LoginDTO;
import com.trace.model.bo.RegisterDTO;
import com.trace.model.vo.LoginResultVO;
import com.trace.model.vo.UserVO;
import com.trace.service.IUserService;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private IUserService userService;

    @PostMapping("/login")
    public Result<LoginResultVO> login(@RequestBody LoginDTO dto) {
        return userService.login(dto);
    }

    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterDTO dto) {
        return userService.register(dto);
    }

    @GetMapping("/user/info")
    public Result<UserVO> getUserInfo(@RequestParam String username) {
        return userService.getUserInfo(username);
    }
}