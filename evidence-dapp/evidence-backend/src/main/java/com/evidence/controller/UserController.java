package com.evidence.controller;

import cn.hutool.crypto.digest.BCrypt;
import com.evidence.common.R;
import com.evidence.common.ResultCode;
import com.evidence.mapper.SysUserMapper;
import com.evidence.model.bo.UpdatePasswordRequest;
import com.evidence.model.bo.UpdateProfileRequest;
import com.evidence.model.entity.SysUserEntity;
import com.evidence.model.vo.UserVO;
import com.evidence.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/profile")
    public R<UserVO> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        SysUserEntity entity = sysUserMapper.selectById(userId);
        if (entity == null) return R.fail(ResultCode.NOT_FOUND);
        UserVO vo = new UserVO();
        vo.setId(entity.getId());
        vo.setUsername(entity.getUsername());
        vo.setRealName(entity.getRealName());
        vo.setPhone(entity.getPhone());
        vo.setEmail(entity.getEmail());
        vo.setAddress(entity.getAddress());
        vo.setAvatarUrl(entity.getAvatarUrl());
        vo.setUserType(entity.getUserType());
        vo.setStatus(entity.getStatus());
        vo.setCreateTime(entity.getCreateTime());
        return R.ok(vo);
    }

    @PutMapping("/profile")
    public R<Void> updateProfile(@RequestBody @Validated UpdateProfileRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        SysUserEntity entity = new SysUserEntity();
        entity.setId(userId);
        entity.setRealName(request.getRealName());
        entity.setPhone(request.getPhone());
        entity.setEmail(request.getEmail());
        entity.setAddress(request.getAddress());
        sysUserMapper.updateById(entity);
        return R.ok();
    }

    @PutMapping("/password")
    public R<Void> updatePassword(@RequestBody @Validated UpdatePasswordRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        SysUserEntity entity = sysUserMapper.selectById(userId);
        if (!BCrypt.checkpw(request.getOldPassword(), entity.getPassword())) {
            return R.fail(ResultCode.PARAM_ERROR.getCode(), "旧密码错误");
        }
        SysUserEntity update = new SysUserEntity();
        update.setId(userId);
        update.setPassword(BCrypt.hashpw(request.getNewPassword(), BCrypt.gensalt()));
        sysUserMapper.updateById(update);
        return R.ok();
    }
}