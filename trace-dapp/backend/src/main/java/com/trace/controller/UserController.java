package com.trace.controller;

import com.trace.model.Result;
import com.trace.model.bo.PasswordChangeDTO;
import com.trace.model.bo.ProfileUpdateDTO;
import com.trace.service.IUserService;
import com.trace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PutMapping("/profile")
    public Result<String> updateProfile(@RequestBody ProfileUpdateDTO dto, HttpServletRequest request) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        String username = jwtUtil.getUsernameFromToken(token);
        return userService.updateProfile(username, dto);
    }

    @PutMapping("/password")
    public Result<String> changePassword(@RequestBody PasswordChangeDTO dto, HttpServletRequest request) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        String username = jwtUtil.getUsernameFromToken(token);
        return userService.changePassword(username, dto);
    }
}