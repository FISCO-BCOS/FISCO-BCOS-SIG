package com.points.service;

import com.points.dto.request.LoginRequest;
import com.points.dto.request.RegisterRequest;
import com.points.dto.response.LoginResponse;
import com.points.dto.response.R;

public interface AuthService {
    R<LoginResponse> login(LoginRequest request);
    R<String> register(RegisterRequest request);
}
