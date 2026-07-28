package com.evidence.service;

import com.evidence.common.R;
import com.evidence.model.bo.LoginRequest;
import com.evidence.model.bo.RegisterRequest;
import com.evidence.model.vo.LoginVO;

import javax.servlet.http.HttpServletResponse;

public interface AuthService {

    R<Long> register(RegisterRequest request);

    R<LoginVO> login(LoginRequest request, HttpServletResponse response);

    R<LoginVO> refreshToken(String refreshToken, HttpServletResponse response);
}
