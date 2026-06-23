package com.points.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.points.dto.request.LoginRequest;
import com.points.dto.request.RegisterRequest;
import com.points.dto.response.LoginResponse;
import com.points.dto.response.R;
import com.points.entity.Merchant;
import com.points.entity.SysUser;
import com.points.enums.AuditStatusEnum;
import com.points.enums.ErrorCodeEnum;
import com.points.enums.UserRoleEnum;
import com.points.exception.BusinessException;
import com.points.mapper.MerchantMapper;
import com.points.mapper.SysUserMapper;
import com.points.service.AuthService;
import com.points.utils.JwtUtil;
import com.points.utils.NoGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public R<LoginResponse> login(LoginRequest request) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername())
        );

        if (user == null) {
            throw new BusinessException(ErrorCodeEnum.USER_NOT_FOUND);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCodeEnum.USER_NOT_FOUND);
        }

        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCodeEnum.USER_PENDING);
        }

        if (user.getStatus() == 2) {
            throw new BusinessException(ErrorCodeEnum.USER_DISABLED);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setRole(user.getRole());
        response.setBlockchainAddress(user.getBlockchainAddress());

        if (user.getRole() == UserRoleEnum.MERCHANT.getCode()) {
            Merchant merchant = merchantMapper.selectOne(
                    new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, user.getId())
            );
            if (merchant != null) {
                response.setMerchantId(merchant.getId());
                response.setMerchantName(merchant.getMerchantName());
            }
        }

        log.info("用户登录成功: username={}, role={}", user.getUsername(), user.getRole());
        return R.ok(response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> register(RegisterRequest request) {
        Long existUser = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername())
        );
        if (existUser > 0) {
            throw new BusinessException(ErrorCodeEnum.USERNAME_EXISTS);
        }

        Long existPhone = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, request.getPhone())
        );
        if (existPhone > 0) {
            throw new BusinessException(ErrorCodeEnum.PHONE_EXISTS);
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(UserRoleEnum.MERCHANT.getCode());
        user.setStatus(0);
        sysUserMapper.insert(user);

        Merchant merchant = new Merchant();
        merchant.setUserId(user.getId());
        merchant.setMerchantName(request.getMerchantName());
        merchant.setMerchantNo(NoGenerator.generateMerchantNo());
        merchant.setContactPerson(request.getContactPerson());
        merchant.setContactPhone(request.getPhone());
        merchant.setAddress(request.getAddress());
        merchant.setBusinessType(request.getBusinessType());
        merchant.setAuditStatus(AuditStatusEnum.PENDING.getCode());
        merchant.setStatus(0);
        merchantMapper.insert(merchant);

        log.info("商户注册成功: username={}, merchantName={}", request.getUsername(), request.getMerchantName());
        return R.ok("注册成功，请等待管理员审核");
    }
}
