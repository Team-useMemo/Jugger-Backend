package com.usememo.jugger.global.security.token.domain.login;

import io.swagger.v3.oas.annotations.media.Schema;

public record NaverLoginRequest(
        @Schema(description = "네이버 인가 코드", example = "dY94g8JaZ1Ki1s...")
        String code) {
}
