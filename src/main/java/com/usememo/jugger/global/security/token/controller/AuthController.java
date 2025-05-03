package com.usememo.jugger.global.security.token.controller;

import static com.fasterxml.jackson.databind.type.LogicalType.*;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.usememo.jugger.domain.user.repository.UserRepository;
import com.usememo.jugger.global.exception.ErrorCode;
import com.usememo.jugger.global.exception.KakaoException;
import com.usememo.jugger.global.security.JwtTokenProvider;
import com.usememo.jugger.global.security.token.domain.KakaoLoginRequest;
import com.usememo.jugger.global.security.token.domain.KakaoSignUpRequest;
import com.usememo.jugger.global.security.token.domain.TokenResponse;
import com.usememo.jugger.global.security.token.repository.RefreshTokenRepository;
import com.usememo.jugger.global.security.token.service.KakaoOAuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Auth API", description = "로그인/로그아웃 API에 대한 설명입니다.")
public class AuthController {
	private final RefreshTokenRepository refreshTokenRepository;
	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final KakaoOAuthService kakaoService;

	@Operation(summary = "[POST] refresh token 으로 새로운 access token 발급")
	@PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> refreshAccessToken(
		@CookieValue(name = "refresh_token", required = false) String refreshToken) {
		if (refreshToken == null) {
			return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("리프레시 토큰이 없습니다"));
		}
		return refreshTokenRepository.findByToken(refreshToken)
			.flatMap(savedToken -> {
				if (!jwtTokenProvider.validateToken(savedToken.getToken())) {
					return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("만료된 리프레시 토큰입니다."));
				}
				String userId = jwtTokenProvider.getUserIdFromToken(savedToken.getToken());
				return userRepository.findById(userId)
					.map(user -> {
						String newAccessToken = jwtTokenProvider.createAccessToken(userId);
						return ResponseEntity.ok().body("{\"accessToken\":\"" + newAccessToken + "\"}");
					});
			})
			.switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("존재하지 않는 리프레시 토큰입니다.")));
	}

	@Operation(summary = "[POST] 로그아웃")
	@PostMapping("/logout")
	public Mono<ResponseEntity<Void>> logout(
		@CookieValue(name = "refresh_token", required = false) String refreshToken) {
		if (refreshToken == null) {
			return Mono.just(ResponseEntity.noContent().build());
		}

		String userId;
		try {
			userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
		} catch (Exception e) {
			return Mono.just(ResponseEntity.noContent().build());
		}
		return refreshTokenRepository.deleteByUserId(UUID.fromString(userId))
			.thenReturn(ResponseEntity
				.noContent()
				.header("Set-Cookie", ResponseCookie.from("refresh_token", "")
					.httpOnly(true)
					.secure(true)
					.path("/")
					.maxAge(0)
					.build()
					.toString()
				)
				.build());
	}

	@Operation(summary = "[POST] 카카오 로그인")
	@PostMapping("/kakao")
	public Mono<ResponseEntity<TokenResponse>> loginByKakao(@RequestBody KakaoLoginRequest request) {
		return kakaoService.loginWithKakao(request.code())
			.map(token -> ResponseEntity.ok().body(token));
	}

	@Operation(summary = "[POST] 회원가입")
	@PostMapping("/kakao/signup")
	public Mono<ResponseEntity<?>> signUpKakao(@RequestBody KakaoSignUpRequest kakaoSignUpRequest){
		return kakaoService.signUpKakao(kakaoSignUpRequest)
			.map(token -> ResponseEntity.ok().body(token));
	}

}
