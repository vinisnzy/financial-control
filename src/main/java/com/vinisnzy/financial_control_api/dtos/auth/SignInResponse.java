package com.vinisnzy.financial_control_api.dtos.auth;

public record SignInResponse(
        String accessToken,
        String refreshToken
) {
}
