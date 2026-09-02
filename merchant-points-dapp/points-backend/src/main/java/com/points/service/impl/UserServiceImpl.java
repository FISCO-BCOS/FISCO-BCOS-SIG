package com.points.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.points.dto.response.R;
import com.points.entity.Merchant;
import com.points.entity.SysUser;
import com.points.enums.ErrorCodeEnum;
import com.points.exception.BusinessException;
import com.points.mapper.MerchantMapper;
import com.points.mapper.SysUserMapper;
import com.points.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public R<Map<String, Object>> getUserProfile(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCodeEnum.NOT_FOUND);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("phone", user.getPhone());
        result.put("email", user.getEmail());
        result.put("role", user.getRole());
        result.put("status", user.getStatus());

        if (user.getRole() == 1) {
            Merchant merchant = merchantMapper.selectOne(
                    new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, userId));
            if (merchant != null) {
                result.put("merchantId", merchant.getId());
                result.put("merchantName", merchant.getMerchantName());
                result.put("merchantNo", merchant.getMerchantNo());
                result.put("auditStatus", merchant.getAuditStatus());
                result.put("pointsBalance", merchant.getPointsBalance());
            }
        }
        return R.ok(result);
    }

    @Override
    public R<String> updatePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCodeEnum.NOT_FOUND);
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(400, "原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserMapper.updateById(user);
        log.info("用户密码修改成功: userId={}", userId);
        return R.ok("密码修改成功");
    }
}
