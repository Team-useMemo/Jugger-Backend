// package com.usememo.jugger.global.security.token.service;
//
// import java.net.URLDecoder;
// import java.nio.charset.StandardCharsets;
// import java.util.HashMap;
// import java.util.Map;
//
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.core.ParameterizedTypeReference;
// import org.springframework.stereotype.Service;
// import org.springframework.web.reactive.function.client.WebClient;
//
// import com.nimbusds.oauth2.sdk.token.RefreshToken;
// import com.usememo.jugger.domain.user.entity.User;
// import com.usememo.jugger.domain.user.repository.UserRepository;
// import com.usememo.jugger.global.exception.BaseException;
// import com.usememo.jugger.global.exception.ErrorCode;
// import com.usememo.jugger.global.security.JwtTokenProvider;
// import com.usememo.jugger.global.security.token.repository.RefreshTokenRepository;
//
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import reactor.core.publisher.Mono;
//
// @Slf4j
// @Service
// @RequiredArgsConstructor
// public class GoogleOAuthService {
// 	private final UserRepository userRepository;
// 	private final RefreshTokenRepository refreshTokenRepository;
// 	private final JwtTokenProvider jwtTokenProvider;
//
// 	@Value("${google.client-id}")
// 	private String clientId;
//
// 	@Value("${google.client-secret}")
// 	private String clientSecret;
//
// 	@Value("${google.redirect-uri}")
// 	private String redirectUri;
//
//
// 	private final WebClient webClient = WebClient.create();
//
// 	public Mono<Map<String, Object>> loginWithGoogle(String code) {
// 		return exchangeCodeForAccessToken(code)
// 			.flatMap(this::fetchUserInfo)
// 			.flatMap(this::handleUserInfo);
// 	}
//
// 	private Mono<String> exchangeCodeForAccessToken(String code) {
// 		String decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8);
//
// 		return webClient.post()
// 			.uri("https://oauth2.googleapis.com/token")
// 			.bodyValue(Map.of(
// 				"code", decodedCode,
// 				"client_id", clientId,
// 				"client_secret", clientSecret,
// 				"redirect_uri", redirectUri,
// 				"grant_type", "authorization_code"
// 			))
// 			.retrieve()
// 			.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
// 			.map(body -> {
// 				if (!body.containsKey("access_token")) {
// 					throw new BaseException(ErrorCode.GOOGLE_LOGIN_FAIL);
// 				}
// 				return (String) body.get("access_token");
// 			});
//
// 	}
//
// 	private Mono<Map<String, Object>> fetchUserInfo(String accessToken) {
// 		return webClient.get()
// 			.uri("https://www.googleapis.com/oauth2/v3/userinfo")
// 			.headers(headers -> headers.setBearerAuth(accessToken))
// 			.retrieve()
// 			.bodyToMono(new ParameterizedTypeReference<>() {});
// 	}
//
// 	private Mono<Map<String, Object>> handleUserInfo(Map<String, Object> userInfo) {
// 		String email = (String) userInfo.get("email");
// 		if (email == null) {
// 			return Mono.error(new BaseException(ErrorCode.GOOGLE_NO_EMAIL));
// 		}
//
// 		return userRepository.findByEmail(email)
// 			.flatMap(user -> {
// 				if (user.getStatus() == UserStatus.SUCCESS) {
// 					return jwtTokenProvider.createTokenBundle(user.getUuid())
// 						.map(tokenResponse ->{
// 							Map<String, Object> result = new HashMap<>();
// 							result.put("accessToken", tokenResponse.getAccessToken());
// 							result.put("isSignedUp", true);
// 							return result;
// 						});
// 				} else {
// 					Map<String, Object> result = new HashMap<>();
// 					result.put("message", "회원가입이 필요합니다");
// 					result.put("email", email);
// 					result.put("isSignedUp", false);
// 					return Mono.just(result);
// 				}
// 			})
// 			.switchIfEmpty(
// 				userRepository.save(User.builder()
// 						.email(email)
// 						// .status(UserStatus.PENDING)
// 						.domain("Google")
// 						.build())
// 					.thenReturn(Map.of(
// 						"message", "회원가입이 필요합니다",
// 						"email", email,
// 						"isSignedUp", false
// 					))
// 			);
// 	}
//
//
// 	// 회원가입 완료 처리
// 	public Mono<Map<String, Object>> completeSignup(SignUpRequest request) {
//
// 		return userRepository.findByEmail(request.email())
// 			.switchIfEmpty(Mono.defer(() -> {
// 				log.warn("해당 이메일 없음: {}", request.email());
// 				return Mono.error(new BaseException(ErrorCode.SIGNUP_ERROR));
// 			}))
// 			.flatMap(user -> {
// 				log.info("유저 발견: {}", user.getEmail());
// 				user.setName(request.name());
// 				user.setWallet_uuid(request.walletUuid());
// 				user.setTerms(request.terms());
// 				user.setStatus(UserStatus.SUCCESS);
// 				log.info("유저 정보 업데이트 완료");
// 				return userRepository.save(user);
// 			})
// 			.flatMap(user -> jwtTokenProvider.createToken(user.getUuid()))
// 			.map(token -> {
// 				log.info("토큰 생성 완료");
// 				return Map.of(
// 					"accessToken", token.accessToken(),
// 					"message", "회원가입 완료"
// 				);
// 			});
// 	}
//
//
//
//
// 	public Mono<Void> logOut(String userId){
// 		return refreshTokenRepository.deleteByUserId(userId);
// 	}
// }
