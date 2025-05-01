package com.usememo.jugger.global.security.token.service;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.usememo.jugger.domain.user.entity.User;
import com.usememo.jugger.domain.user.repository.UserRepository;
import com.usememo.jugger.global.security.JwtTokenProvider;
import com.usememo.jugger.global.security.token.domain.KakaoOAuthProperties;
import com.usememo.jugger.global.security.token.domain.KakaoUserResponse;
import com.usememo.jugger.global.security.token.domain.TokenResponse;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

	private final WebClient webClient = WebClient.create();
	private final KakaoOAuthProperties kakaoProps;
	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;

	public Mono<TokenResponse> loginWithKakao(String code) {
		return getAccessToken(code)
			.flatMap(this::getUserInfo)
			.flatMap(this::saveOrFindUser)
			.map(user -> jwtTokenProvider.createTokenBundle(user.getUuid()));
	}

	private Mono<String> getAccessToken(String code) {
		return webClient.post()
			.uri("https://kauth.kakao.com/oauth/token")
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(BodyInserters.fromFormData("grant_type", "authorization_code")
				.with("client_id", kakaoProps.getClientId())
				.with("redirect_uri", kakaoProps.getRedirectUri())
				.with("code", code))
			.retrieve()
			.bodyToMono(Map.class)
			.map(body -> (String) body.get("access_token"));
	}

	private Mono<KakaoUserResponse> getUserInfo(String accessToken) {
		return webClient.get()
			.uri("https://kapi.kakao.com/v2/user/me")
			.headers(headers -> headers.setBearerAuth(accessToken))
			.retrieve()
			.bodyToMono(KakaoUserResponse.class);
	}

	private Mono<User> saveOrFindUser(KakaoUserResponse response) {
		String email = response.getKakaoAccount().getEmail();
		String name = response.getProperties().getNickname();

		return userRepository.findByEmail(email)
			.switchIfEmpty(userRepository.save(User.builder()
				.email(email)
				.name(name)
				.domain("kakao")
				.terms(new User.Terms()) // 약관은 따로 받으면 됨
				.build()));
	}
}
