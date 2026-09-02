package com.evidence.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.evidence.common.R;
import com.evidence.common.ResultCode;
import com.evidence.mapper.SysUserMapper;
import com.evidence.model.bo.LoginRequest;
import com.evidence.model.bo.RegisterRequest;
import com.evidence.model.entity.SysUserEntity;
import com.evidence.model.vo.LoginVO;
import com.evidence.service.AuthService;
import com.evidence.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public R<Long> register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return R.fail(ResultCode.PARAM_ERROR.getCode(), "两次密码不一致");
        }

        LambdaQueryWrapper<SysUserEntity> usernameWrapper = new LambdaQueryWrapper<>();
        usernameWrapper.eq(SysUserEntity::getUsername, request.getUsername());
        if (sysUserMapper.selectOne(usernameWrapper) != null) {
            return R.fail(ResultCode.CONFLICT.getCode(), "用户名已存在");
        }

        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            LambdaQueryWrapper<SysUserEntity> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(SysUserEntity::getPhone, request.getPhone());
            if (sysUserMapper.selectOne(phoneWrapper) != null) {
                return R.fail(ResultCode.CONFLICT.getCode(), "手机号已注册");
            }
        }

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            LambdaQueryWrapper<SysUserEntity> emailWrapper = new LambdaQueryWrapper<>();
            emailWrapper.eq(SysUserEntity::getEmail, request.getEmail());
            if (sysUserMapper.selectOne(emailWrapper) != null) {
                return R.fail(ResultCode.CONFLICT.getCode(), "邮箱已注册");
            }
        }

        SysUserEntity user = new SysUserEntity();
        user.setUsername(request.getUsername());
        user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setUserType(request.getUserType());
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        sysUserMapper.insert(user);
        return R.ok(user.getId());
    }

    @Override
    public R<LoginVO> login(LoginRequest request, HttpServletResponse response) {
        SysUserEntity user = findByUsernameOrPhoneOrEmail(request.getUsername());
        if (user == null) {
            return R.fail(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }

        if (user.getStatus() != 1) {
            return R.fail(ResultCode.FORBIDDEN.getCode(), "账户已被禁用");
        }

        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            return R.fail(ResultCode.PARAM_ERROR.getCode(), "密码错误");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        response.setHeader("Set-Cookie",
                "refresh_token=" + refreshToken +
                "; Path=/; HttpOnly; SameSite=Strict; Max-Age=" + jwtUtil.getRefreshExpirationSeconds());

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(accessToken);
        loginVO.setExpiresIn(jwtUtil.getAccessExpirationSeconds());

        LoginVO.UserInfo userInfo = new LoginVO.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setAvatarUrl(user.getAvatarUrl());
        userInfo.setUserType(user.getUserType());
        loginVO.setUser(userInfo);

        return R.ok(loginVO);
    }

    private SysUserEntity findByUsernameOrPhoneOrEmail(String input) {
        LambdaQueryWrapper<SysUserEntity> usernameWrapper = new LambdaQueryWrapper<>();
        usernameWrapper.eq(SysUserEntity::getUsername, input);
        SysUserEntity user = sysUserMapper.selectOne(usernameWrapper);
        if (user != null) {
            return user;
        }

        LambdaQueryWrapper<SysUserEntity> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.eq(SysUserEntity::getPhone, input);
        user = sysUserMapper.selectOne(phoneWrapper);
        if (user != null) {
            return user;
        }

        LambdaQueryWrapper<SysUserEntity> emailWrapper = new LambdaQueryWrapper<>();
        emailWrapper.eq(SysUserEntity::getEmail, input);
        return sysUserMapper.selectOne(emailWrapper);
    }

    @Override
    public R<LoginVO> refreshToken(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return R.fail(ResultCode.UNAUTHORIZED.getCode(), "缺少刷新令牌");
        }
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            return R.fail(ResultCode.UNAUTHORIZED.getCode(), "无效的刷新令牌");
        }
        if (jwtUtil.isTokenExpired(refreshToken)) {
            response.setHeader("Set-Cookie", "refresh_token=; Path=/; HttpOnly; Max-Age=0");
            return R.fail(ResultCode.UNAUTHORIZED.getCode(), "刷新令牌已过期，请重新登录");
        }
        try {
            Long userId = jwtUtil.parseToken(refreshToken).getClaim("userId").asLong();
            String username = jwtUtil.parseToken(refreshToken).getClaim("username").asString();
            SysUserEntity user = sysUserMapper.selectById(userId);
            if (user == null || user.getStatus() != 1) {
                return R.fail(ResultCode.UNAUTHORIZED.getCode(), "用户不存在或已禁用");
            }
            String newAccessToken = jwtUtil.generateAccessToken(userId, username);
            String newRefreshToken = jwtUtil.generateRefreshToken(userId, username);
            response.setHeader("Set-Cookie",
                    "refresh_token=" + newRefreshToken +
                    "; Path=/; HttpOnly; SameSite=Strict; Max-Age=" + jwtUtil.getRefreshExpirationSeconds());
            LoginVO loginVO = new LoginVO();
            loginVO.setToken(newAccessToken);
            loginVO.setExpiresIn(jwtUtil.getAccessExpirationSeconds());
            LoginVO.UserInfo userInfo = new LoginVO.UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setRealName(user.getRealName());
            userInfo.setAvatarUrl(user.getAvatarUrl());
            userInfo.setUserType(user.getUserType());
            loginVO.setUser(userInfo);
            return R.ok(loginVO);
        } catch (Exception e) {
            return R.fail(ResultCode.UNAUTHORIZED.getCode(), "令牌验证失败");
        }
    }
}
