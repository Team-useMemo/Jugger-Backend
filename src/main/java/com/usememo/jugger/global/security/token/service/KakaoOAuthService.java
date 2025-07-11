package com.usememo.jugger.global.security.token.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usememo.jugger.domain.user.entity.User;
import com.usememo.jugger.domain.user.repository.UserRepository;
import com.usememo.jugger.global.exception.BaseException;
import com.usememo.jugger.global.exception.ErrorCode;
import com.usememo.jugger.global.exception.KakaoException;
import com.usememo.jugger.global.security.JwtTokenProvider;
import com.usememo.jugger.global.security.token.domain.KakaoOAuthProperties;
import com.usememo.jugger.global.security.token.domain.KakaoSignUpRequest;
import com.usememo.jugger.global.security.token.domain.KakaoUserResponse;
import com.usememo.jugger.global.security.token.domain.NewTokenResponse;
import com.usememo.jugger.global.security.token.domain.TokenResponse;
import com.usememo.jugger.global.security.token.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOAuthService {
	private final WebClient webClient = WebClient.create();
	private final KakaoOAuthProperties kakaoProps;
	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenRepository refreshTokenRepository;

	public Mono<TokenResponse> loginWithKakao(String code) {
		return getAccessToken(code)
			.flatMap(this::getUserInfo)
			.flatMap(this::saveOrFindUser)
			.flatMap(user -> {
					return jwtTokenProvider.createTokenBundle(user.getUuid());
				}

			);
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
			.onStatus(status -> !status.is2xxSuccessful(),
				response -> Mono.error(new BaseException(ErrorCode.KAKAO_TOKEN_REQUEST_FAILED)))
			.bodyToMono(Map.class)
			.map(body -> {
				String token = (String)body.get("access_token");
				if (token == null) {
					throw new BaseException(ErrorCode.KAKAO_TOKEN_MISSING);
				}
				return token;
			})
			.onErrorMap(e -> new BaseException(ErrorCode.KAKAO_CONNECTION_FAILED));
	}

	private Mono<KakaoUserResponse> getUserInfo(String accessToken) {

		return webClient.get()
			.uri("https://kapi.kakao.com/v2/user/me")
			.headers(headers -> headers.setBearerAuth(accessToken))
			.retrieve()
			.bodyToMono(String.class)
			.flatMap(raw -> {
				try {
					KakaoUserResponse parsed = new ObjectMapper().readValue(raw, KakaoUserResponse.class);
					return Mono.just(parsed);
				} catch (Exception e) {
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

		return userRepository.findByEmailAndDomain(email, "kakao")
			.switchIfEmpty(Mono.defer(() -> {
				return Mono.error(new KakaoException(ErrorCode.USER_NOT_FOUND,
					Map.of("email", email, "nickname", name)));
			}));
	}

	public Mono<TokenResponse> signUpKakao(KakaoSignUpRequest kakaoSignUpRequest) {
		String email = kakaoSignUpRequest.email();
		String domain = kakaoSignUpRequest.domain();
		String name = kakaoSignUpRequest.name();

		return userRepository.findByEmailAndDomainAndName(email, domain, name)
			.flatMap(existingUser ->
				Mono.<TokenResponse>error(new BaseException(ErrorCode.DUPLICATE_USER))
			)
			.switchIfEmpty(Mono.defer(() -> {
				String uuid = UUID.randomUUID().toString();

				User.Terms terms = new User.Terms();
				terms.setMarketing(kakaoSignUpRequest.terms().isMarketing());
				terms.setPrivacyPolicy(kakaoSignUpRequest.terms().isPrivacyPolicy());
				terms.setTermsOfService(kakaoSignUpRequest.terms().isTermsOfService());

				User user = User.builder()
					.uuid(uuid)
					.name(name)
					.email(email)
					.domain(domain)
					.terms(terms)
					.build();

				return userRepository.save(user)
					.flatMap(savedUser -> jwtTokenProvider.createTokenBundle(savedUser.getUuid()));
			}));
	}

	public Mono<Void> userLogOut(String refreshToken) {
		String userId;
		userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
		if (userId == null) {
			throw new BaseException(ErrorCode.NO_LOGOUT_USER);
		}
		return refreshTokenRepository.findByUserId(userId)
			.switchIfEmpty(Mono.error(new BaseException(ErrorCode.NO_LOGOUT_USER)))
			.flatMap(foundToken -> refreshTokenRepository.deleteByUserId(userId));
	}

	public Mono<ResponseEntity<NewTokenResponse>> giveNewToken(String refreshToken) {
		return refreshTokenRepository.findByToken(refreshToken)
			.flatMap(savedToken -> {
				if (!jwtTokenProvider.validateToken(savedToken.getToken())) {
					return Mono.error(new BaseException(ErrorCode.NO_REFRESH_TOKEN));
				}
				String userId = jwtTokenProvider.getUserIdFromToken(savedToken.getToken());
				return userRepository.findById(userId)
					.map(user -> {
						String newAccessToken = jwtTokenProvider.createAccessToken(userId);
						return ResponseEntity.ok().body(new NewTokenResponse(newAccessToken));
					});
			})
			.switchIfEmpty(Mono.error(new BaseException(ErrorCode.NO_REFRESH_TOKEN)));
	}

}
