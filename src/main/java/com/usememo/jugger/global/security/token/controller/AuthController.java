package com.usememo.jugger.global.security.token.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.usememo.jugger.global.exception.BaseException;
import com.usememo.jugger.global.exception.ErrorCode;
import com.usememo.jugger.global.security.token.domain.GoogleLoginRequest;
import com.usememo.jugger.global.security.token.domain.GoogleSignupRequest;
import com.usememo.jugger.global.security.token.domain.KakaoLoginRequest;
import com.usememo.jugger.global.security.token.domain.KakaoSignUpRequest;
import com.usememo.jugger.global.security.token.domain.LogOutRequest;
import com.usememo.jugger.global.security.token.domain.LogOutResponse;
import com.usememo.jugger.global.security.token.domain.NewTokenResponse;
import com.usememo.jugger.global.security.token.domain.RefreshTokenRequest;
import com.usememo.jugger.global.security.token.domain.TokenResponse;
import com.usememo.jugger.global.security.token.service.GoogleOAuthService;
import com.usememo.jugger.global.security.token.service.KakaoOAuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "로그인 API", description = "로그인/로그아웃 API에 대한 설명입니다.")
public class AuthController {

	private final KakaoOAuthService kakaoService;
	private final GoogleOAuthService googleOAuthService;

	@Operation(summary = "[POST] refresh token으로 새로운 access token 발급")
	@PostMapping(value = "/refresh")
	public Mono<ResponseEntity<NewTokenResponse>> refreshAccessToken(@RequestBody RefreshTokenRequest request) {
		String refreshToken = request.refreshToken();

		if (refreshToken == null || refreshToken.isBlank()) {
			throw new BaseException(ErrorCode.NO_REFRESH_TOKEN);
		}
		return kakaoService.giveNewToken(refreshToken);
	}

	@Operation(summary = "[POST] 로그아웃")
	@PostMapping("/logout")
	public Mono<ResponseEntity<LogOutResponse>> logout(@RequestBody LogOutRequest request) {

		return kakaoService.userLogOut(request.refreshToken())
			.thenReturn(ResponseEntity.ok().body(new LogOutResponse("로그아웃이 성공적으로 되었습니다.")));
	}

	@Operation(summary = "[POST] 카카오 로그인")
	@PostMapping("/kakao")
	public Mono<ResponseEntity<TokenResponse>> loginByKakao(@RequestBody KakaoLoginRequest request) {
		return kakaoService.loginWithKakao(request.code())
			.map(token -> ResponseEntity.ok().body(token));
	}

	@Operation(summary = "[POST] 카카오 회원가입")
	@PostMapping("/kakao/signup")
	public Mono<ResponseEntity<TokenResponse>> signUpKakao(@RequestBody KakaoSignUpRequest kakaoSignUpRequest) {
		return kakaoService.signUpKakao(kakaoSignUpRequest)
			.map(token -> ResponseEntity.ok().body(token));

	}

	@Operation(summary = "[POST] 구글 로그인")
	@PostMapping("/google")
	public Mono<ResponseEntity<TokenResponse>> loginByGoogle(@RequestBody GoogleLoginRequest googleLoginRequest) {
		return googleOAuthService.loginWithGoogle(googleLoginRequest.code())
			.map(token -> ResponseEntity.ok().body(token));
	}

	@Operation(summary = "[POST] 구글 회원가입")
	@PostMapping("/google/signup")
	public Mono<ResponseEntity<TokenResponse>> signUpGoogle(@RequestBody GoogleSignupRequest googleSignupRequest) {
		return googleOAuthService.signUpGoogle(googleSignupRequest)
			.map(token -> ResponseEntity.ok().body(token));
	}

}
