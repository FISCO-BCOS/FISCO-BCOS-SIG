package com.points.service;

import com.points.dto.response.R;
import java.util.Map;

public interface UserService {
    R<Map<String, Object>> getUserProfile(Long userId);
    R<String> updatePassword(Long userId, String oldPassword, String newPassword);
}
