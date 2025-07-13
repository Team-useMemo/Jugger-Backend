package com.usememo.jugger.global.security.token.domain.userResponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class KakaoUserResponse {
	private Long id;
	private KakaoAccount kakao_account;
	private Properties properties;

	@Getter @Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class KakaoAccount {
		private String email;
	}

	@Getter @Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Properties {
		private String nickname;
	}
}
