package com.usememo.jugger.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.usememo.jugger.global.exception.BaseException;
import com.usememo.jugger.global.exception.ErrorCode;
import com.usememo.jugger.global.security.CustomOAuth2User;
import com.usememo.jugger.global.security.token.domain.KakaoLogoutResponse;
import com.usememo.jugger.global.security.token.service.KakaoOAuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/user")
@Tag(name = "User API", description = "회원정보 수정 및 회원탈퇴 API에 대한 설명입니다.")
public class UserController {

	private final KakaoOAuthService kakaoOAuthService;

	@Operation(summary = "[DELETE] 회원탈퇴")
	@DeleteMapping("/kakao/signout")
	public Mono<ResponseEntity<?>> deleteUser(@AuthenticationPrincipal CustomOAuth2User customOAuth2User){
		return kakaoOAuthService.deleteUser(customOAuth2User.getUserId())
			.then(Mono.fromCallable(() -> ResponseEntity.ok().body(new KakaoLogoutResponse(200,"회원탈퇴에 성공하였습니다."))));
	}
}
