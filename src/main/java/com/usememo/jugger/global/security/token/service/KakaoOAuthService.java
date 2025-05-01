package com.usememo.jugger.global.security.token.service;

import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usememo.jugger.domain.user.entity.User;
import com.usememo.jugger.domain.user.repository.UserRepository;
import com.usememo.jugger.global.exception.BaseException;
import com.usememo.jugger.global.exception.ErrorCode;
import com.usememo.jugger.global.security.JwtTokenProvider;
import com.usememo.jugger.global.security.token.domain.KakaoOAuthProperties;
import com.usememo.jugger.global.security.token.domain.KakaoUserResponse;
import com.usememo.jugger.global.security.token.domain.TokenResponse;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOAuthService {
	private final WebClient webClient = WebClient.create();
	private final KakaoOAuthProperties kakaoProps;
	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;

	public Mono<TokenResponse> loginWithKakao(String code) {
		log.info("user : ");
		return getAccessToken(code)
			.flatMap(this::getUserInfo)
			.flatMap(this::saveOrFindUser)
			.map(user -> {
					log.info("user : "+user);
					return jwtTokenProvider.createTokenBundle(user.getUuid());
				}
			)
			.onErrorMap(e -> new BaseException(ErrorCode.KAKAO_JWT_ERROR));
	}

	private Mono<String> getAccessToken(String code) {
		log.info("✅ getAccessToken - 시작");

		return webClient.post()
			.uri("https://kauth.kakao.com/oauth/token")
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(BodyInserters.fromFormData("grant_type", "authorization_code")
				.with("client_id", kakaoProps.getClientId())
				.with("redirect_uri", kakaoProps.getRedirectUri())
				.with("code", code))
			.retrieve()
			.onStatus(status -> {
				log.warn("⚠️ 응답 상태코드: {}", status);
				return !status.is2xxSuccessful();
			}, response -> {
				log.error("❌ 상태코드 에러 발생");
				return Mono.error(new BaseException(ErrorCode.KAKAO_TOKEN_REQUEST_FAILED));
			})
			.bodyToMono(Map.class)
			.doOnNext(body -> log.info("✅ 응답 body: {}", body))
			.map(body -> {
				String token = (String) body.get("access_token");
				if (token == null) {
					log.error("❌ access_token 누락");
					throw new BaseException(ErrorCode.KAKAO_TOKEN_MISSING);
				}
				log.info("✅ access_token: {}", token);
				return token;
			})
			.doOnError(e -> log.error("❌ getAccessToken 내부 에러 발생", e))
			.onErrorMap(e -> new BaseException(ErrorCode.KAKAO_CONNECTION_FAILED));
	}


	private Mono<KakaoUserResponse> getUserInfo(String accessToken) {
		log.info("here2");

		return webClient.get()
			.uri("https://kapi.kakao.com/v2/user/me")
			.headers(headers -> headers.setBearerAuth(accessToken))
			.retrieve()
			.bodyToMono(String.class)  // 문자열로 먼저 받기
			.doOnNext(raw -> log.info("🔥 Kakao raw response: {}", raw))
			.flatMap(raw -> {
				try {
					KakaoUserResponse parsed = new ObjectMapper().readValue(raw, KakaoUserResponse.class);
					return Mono.just(parsed);
				} catch (Exception e) {
					log.error("❌ JSON 파싱 실패", e);
					return Mono.error(new BaseException(ErrorCode.KAKAO_JSON_PARSE_ERROR));
				}
			});
	}


	private Mono<User> saveOrFindUser(KakaoUserResponse response) {
		String email = response.getKakao_account().getEmail();
		String name = response.getProperties().getNickname();

		if (email == null) {
			return Mono.error(new BaseException(ErrorCode.KAKAO_EMAIL_MISSING));
		}
		if (name == null) {
			return Mono.error(new BaseException(ErrorCode.KAKAO_NAME_MISSING));
		}
		log.info("here3");
		return userRepository.findByEmail(email)
			.switchIfEmpty(Mono.defer(() ->
				userRepository.save(User.builder()
					.uuid(UUID.randomUUID().toString())
					.email(email)
					.name(name)
					.domain("kakao")
					.terms(new User.Terms())
					.build())


			))
			.doOnNext(savedUser -> log.info("Saved user: " + savedUser))
			.doOnError(err -> log.error("❌ 저장 중 에러 발생", err))
			.onErrorMap(e -> new BaseException(ErrorCode.KAKAO_UNKNOWN_ERROR));
	}
}
