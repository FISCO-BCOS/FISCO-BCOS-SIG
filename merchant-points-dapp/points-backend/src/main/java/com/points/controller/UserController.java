package com.points.controller;

import com.points.dto.response.R;
import com.points.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Api(tags = "用户管理")
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @ApiOperation("获取个人信息")
    @GetMapping("/profile")
    public R<Map<String, Object>> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.getUserProfile(userId);
    }

    @ApiOperation("修改密码")
    @PutMapping("/password")
    public R<String> updatePassword(HttpServletRequest request, @RequestBody Map<String, String> body) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.updatePassword(userId, body.get("oldPassword"), body.get("newPassword"));
    }
}
