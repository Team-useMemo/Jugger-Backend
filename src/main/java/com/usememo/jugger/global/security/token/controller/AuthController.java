package com.usememo.jugger.global.security.token.controller;

import com.usememo.jugger.global.security.token.domain.logOutResponse.LogOutRequest;
import com.usememo.jugger.global.security.token.domain.logOutResponse.LogOutResponse;
import com.usememo.jugger.global.security.token.domain.login.GoogleLoginRequest;
import com.usememo.jugger.global.security.token.domain.login.KakaoLoginRequest;
import com.usememo.jugger.global.security.token.domain.login.NaverLoginRequest;
import com.usememo.jugger.global.security.token.domain.signUp.GoogleSignupRequest;
import com.usememo.jugger.global.security.token.domain.signUp.KakaoSignUpRequest;
import com.usememo.jugger.global.security.token.domain.signUp.NaverSignUpRequest;
import com.usememo.jugger.global.security.token.domain.token.NewTokenResponse;
import com.usememo.jugger.global.security.token.domain.token.RefreshTokenRequest;
import com.usememo.jugger.global.security.token.domain.token.TokenResponse;
import com.usememo.jugger.global.security.token.service.NaverOAuthService;
import com.usememo.jugger.global.security.token.service.OAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.usememo.jugger.global.exception.BaseException;
import com.usememo.jugger.global.exception.ErrorCode;
import com.usememo.jugger.global.security.token.domain.GoogleLoginRequest;
import com.usememo.jugger.global.security.token.domain.KakaoLoginRequest;
import com.usememo.jugger.global.security.token.domain.SignUpRequest;
import com.usememo.jugger.global.security.token.domain.LogOutRequest;
import com.usememo.jugger.global.security.token.domain.LogOutResponse;
import com.usememo.jugger.global.security.token.domain.NewTokenResponse;
import com.usememo.jugger.global.security.token.domain.RefreshTokenRequest;
import com.usememo.jugger.global.security.token.domain.TokenResponse;
import com.usememo.jugger.global.security.token.service.GoogleOAuthService;
import com.usememo.jugger.global.security.token.service.KakaoOAuthService;
import com.usememo.jugger.global.security.token.service.SignService;

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
	private final SignService signService;

    @Operation(summary = "[POST] refresh token으로 새로운 access token 발급")
    @PostMapping(value = "/refresh")
    public Mono<ResponseEntity<NewTokenResponse>> refreshAccessToken(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();
        String provider = request.provider();


		if (refreshToken == null || refreshToken.isBlank()) {
			throw new BaseException(ErrorCode.NO_REFRESH_TOKEN);
		}
		return signService.giveNewToken(refreshToken);
	}



    @Operation(summary = "[POST] 로그아웃")
    @PostMapping("/logout")
    public Mono<ResponseEntity<LogOutResponse>> logout(@RequestBody LogOutRequest request) {
        String provider = request.provider();
         return getOAuthService(provider).userLogOut(request.refreshToken())
                .thenReturn(ResponseEntity.ok().body(new LogOutResponse("로그아웃이 성공적으로 되었습니다.")));
    }

	@Operation(summary = "[POST] 회원가입")
	@PostMapping("/signup")
	public Mono<ResponseEntity<TokenResponse>> signUpKakao(@RequestBody SignUpRequest signUpRequest) {
		return signService.signUp(signUpRequest)
			.map(token -> ResponseEntity.ok().body(token));


    @Operation(summary = "[POST] 카카오 로그인")
    @PostMapping("/kakao")
    public Mono<ResponseEntity<TokenResponse>> loginByKakao(@RequestBody KakaoLoginRequest request) {
        return kakaoService.loginWithKakao(request.code())
                .map(token -> ResponseEntity.ok().body(token));
    }


    @Operation(summary = "[POST] 구글 로그인")
    @PostMapping("/google")
    public Mono<ResponseEntity<TokenResponse>> loginByGoogle(@RequestBody GoogleLoginRequest googleLoginRequest) {
        return googleOAuthService.loginWithGoogle(googleLoginRequest.code())
                .map(token -> ResponseEntity.ok().body(token));
    }



    @Operation(summary = "[POST] 네이버 로그인")
    @PostMapping("/naver")
    public Mono<ResponseEntity<TokenResponse>> loginByNaver(@RequestBody NaverLoginRequest naverLoginRequest) {
        return naverOAuthService.loginWithNaver(naverLoginRequest.code())
                .map(token -> ResponseEntity.ok().body(token));
    }
	@Operation(summary = "[POST] 구글 로그인")
	@PostMapping("/google")
	public Mono<ResponseEntity<TokenResponse>> loginByGoogle(@RequestBody GoogleLoginRequest googleLoginRequest) {
		return googleOAuthService.loginWithGoogle(googleLoginRequest.code())
			.map(token -> ResponseEntity.ok().body(token));
	}


    @Operation(summary = "[POST] 네이버 회원가입")
    @PostMapping("/naver")
    public Mono<ResponseEntity<TokenResponse>> signUpNaver(@RequestBody NaverSignUpRequest naverSignUpRequest) {
        return naverOAuthService.signUpNaver(naverSignUpRequest)
                .map(token -> ResponseEntity.ok().body(token));
    }

    private OAuthService getOAuthService(String provider) {
        return switch (provider.toLowerCase()) {
            case "kakao" -> kakaoService;
            case "google" -> googleOAuthService;
            case "naver" -> naverOAuthService;
            default -> throw new BaseException(ErrorCode.UNSUPPORTED_PROVIDER);
        };
    }


}
