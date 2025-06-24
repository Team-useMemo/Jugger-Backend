package com.usememo.jugger.global.security.token.domain;

import io.swagger.v3.oas.annotations.media.Schema;

public record GoogleLoginRequest(@Schema(description = "구글 인가 코드", example = "dY94g8JaZ1Ki1s...")
								 String code) {
}
