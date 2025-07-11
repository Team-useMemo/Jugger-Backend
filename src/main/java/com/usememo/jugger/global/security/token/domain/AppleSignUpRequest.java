package com.usememo.jugger.global.security.token.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

public record AppleSignUpRequest(@Schema(description = "사용자 이름", example = "홍길동", required = true)
                                 String name,

                                 @Schema(description = "이메일 주소", example = "hong@example.com", required = true)
                                 String email,

                                 @Schema(description = "약관 동의 정보", required = true)
                                 KakaoSignUpRequest.Terms terms
) {
    @Schema(name = "Terms", description = "약관 동의 항목들")
    @Data
    public static class Terms {
        @Schema(description = "서비스 이용약관 동의 여부", example = "true", required = true)
        private boolean termsOfService;

        @Schema(description = "개인정보 처리방침 동의 여부", example = "true", required = true)
        private boolean privacyPolicy;

        @Schema(description = "마케팅 정보 수신 동의 여부", example = "false", required = true)
        private boolean marketing;
    }
}