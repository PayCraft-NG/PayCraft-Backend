package com.aalto.paycraft.services.impl;

import com.aalto.paycraft.dto.AuthorizationResponseDto;
import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.LoginRequestDto;
import com.aalto.paycraft.dto.RefreshTokenRequestDto;
import com.aalto.paycraft.services.IAuthenticationService;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements IAuthenticationService {
    @Override
    public DefaultApiResponse<AuthorizationResponseDto> login(LoginRequestDto requestBody) {
        return null;
    }

    @Override
    public DefaultApiResponse<AuthorizationResponseDto> refreshToken(RefreshTokenRequestDto requestBody) {
        return null;
    }
}
