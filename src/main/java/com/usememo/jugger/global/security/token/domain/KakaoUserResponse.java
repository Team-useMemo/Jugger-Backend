package com.usememo.jugger.global.security.token.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoUserResponse {
	private Long id;
	private KakaoAccount kakaoAccount;
	private Properties properties;

	@Getter @Setter
	public static class KakaoAccount {
		private String email;
	}

	@Getter @Setter
	public static class Properties {
		private String nickname;
	}
}
