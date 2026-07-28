package com.trace.service.impl;

import com.trace.mapper.UserMapper;
import com.trace.model.Result;
import com.trace.model.User;
import com.trace.model.bo.LoginDTO;
import com.trace.model.bo.PasswordChangeDTO;
import com.trace.model.bo.ProfileUpdateDTO;
import com.trace.model.bo.RegisterDTO;
import com.trace.model.vo.LoginResultVO;
import com.trace.model.vo.ResultVO;
import com.trace.model.vo.UserVO;
import com.trace.service.IUserService;
import com.trace.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";

    @Override
    public Result<String> updateProfile(String username, ProfileUpdateDTO dto) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            return Result.error(ResultVO.USER_NOT_EXIST);
        }
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setOrganization(dto.getOrganization());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
        return Result.success("资料更新成功");
    }

    @Override
    public Result<String> changePassword(String username, PasswordChangeDTO dto) {
        if (!StringUtils.hasText(dto.getOldPassword()) || !StringUtils.hasText(dto.getNewPassword())) {
            return Result.error(ResultVO.PARAM_EMPTY);
        }
        User user = userMapper.findByUsername(username);
        if (user == null) {
            return Result.error(ResultVO.USER_NOT_EXIST);
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            return new Result<>(400, "原密码不正确", null);
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
        return Result.success("密码修改成功");
    }

    @Override
    public Result<LoginResultVO> login(LoginDTO dto) {
        if (!StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            return Result.error(ResultVO.PARAM_EMPTY);
        }

        User user = userMapper.findByUsername(dto.getUsername());
        if (user == null) {
            return Result.error(ResultVO.USER_NOT_EXIST);
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return Result.error(ResultVO.PASSWORD_ERROR);
        }

        if (user.getStatus() != 1) {
            return Result.error(ResultVO.TOKEN_EMPTY);
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), user.getId());

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        LoginResultVO loginResult = new LoginResultVO();
        loginResult.setToken(token);
        loginResult.setTokenType("Bearer");
        loginResult.setExpiresIn(86400L);
        loginResult.setUser(userVO);

        return Result.success(loginResult);
    }

    @Override
    public Result<String> register(RegisterDTO dto) {
        if (!StringUtils.hasText(dto.getUsername()) ||
            !StringUtils.hasText(dto.getPassword()) ||
            !StringUtils.hasText(dto.getConfirmPassword()) ||
            !StringUtils.hasText(dto.getRealName()) ||
            !StringUtils.hasText(dto.getPhone()) ||
            dto.getRole() == null) {
            return Result.error(ResultVO.PARAM_EMPTY);
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            return new Result<>(400, "两次密码输入不一致", null);
        }

        if (!Pattern.matches(PHONE_REGEX, dto.getPhone())) {
            return new Result<>(400, "手机号格式不正确", null);
        }

        User existing = userMapper.findByUsername(dto.getUsername());
        if (existing != null) {
            return Result.error(ResultVO.QUERY_EXISTS);
        }

        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.insert(user);

        return Result.success(user.getId().toString());
    }

    @Override
    public Result<UserVO> getUserInfo(String username) {
        if (!StringUtils.hasText(username)) {
            return Result.error(ResultVO.PARAM_EMPTY);
        }

        User user = userMapper.findByUsername(username);
        if (user == null) {
            return Result.error(ResultVO.USER_NOT_EXIST);
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        return Result.success(userVO);
    }

    @Override
    public List<UserVO> listByRole(Integer role) {
        return userMapper.findByRole(role).stream().map(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}