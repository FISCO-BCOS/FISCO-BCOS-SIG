package com.trace.controller;

import com.trace.model.Result;
import com.trace.model.vo.DashboardVO;
import com.trace.model.vo.UserVO;
import com.trace.service.IProductService;
import com.trace.service.IUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private IProductService productService;

    @Autowired
    private IUserService userService;

    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        return productService.getDashboard();
    }

    @GetMapping("/farmers/list")
    public Result<List<UserVO>> listFarmers() {
        return Result.success(userService.listByRole(1));
    }

    @GetMapping("/processors/list")
    public Result<List<UserVO>> listProcessors() {
        return Result.success(userService.listByRole(2));
    }
}