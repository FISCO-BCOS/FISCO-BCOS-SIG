package com.trace.service;

import com.trace.model.Result;
import com.trace.model.bo.LoginDTO;
import com.trace.model.bo.PasswordChangeDTO;
import com.trace.model.bo.ProfileUpdateDTO;
import com.trace.model.bo.RegisterDTO;
import com.trace.model.vo.LoginResultVO;
import com.trace.model.vo.UserVO;

import java.util.List;

public interface IUserService {

    Result<LoginResultVO> login(LoginDTO dto);

    Result<String> register(RegisterDTO dto);

    Result<UserVO> getUserInfo(String username);

    List<UserVO> listByRole(Integer role);

    Result<String> updateProfile(String username, ProfileUpdateDTO dto);

    Result<String> changePassword(String username, PasswordChangeDTO dto);
}