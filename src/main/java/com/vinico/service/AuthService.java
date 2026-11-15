package com.vinico.service;

import com.vinico.dto.request.LoginRequest;
import com.vinico.dto.request.RegistroRequest;
import com.vinico.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse registro(RegistroRequest request);
    AuthResponse login(LoginRequest request);
}
