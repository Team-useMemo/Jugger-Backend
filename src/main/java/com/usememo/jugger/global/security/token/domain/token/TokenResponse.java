package com.usememo.jugger.global.security.token.domain.token;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponse {
	private String accessToken;
	private String refreshToken;
	private String email;
}
