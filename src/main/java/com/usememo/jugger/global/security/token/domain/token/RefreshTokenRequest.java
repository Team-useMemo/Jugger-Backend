package com.usememo.jugger.global.security.token.domain.token;

import io.swagger.v3.oas.annotations.media.Schema;

public record RefreshTokenRequest(
        String refreshToken,
        @Schema(description = "OAuth 제공자", example = "kakao", allowableValues = {"kakao", "google", "naver"})
        String provider
) {}
