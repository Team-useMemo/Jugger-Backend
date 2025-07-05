package com.usememo.jugger.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.usememo.jugger.domain.user.LogoutResponse;
import com.usememo.jugger.domain.user.dto.WithdrawalRequest;
import com.usememo.jugger.domain.user.service.UserService;
import com.usememo.jugger.global.security.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/user")
@Tag(name = "유저 API", description = "회원정보 수정 및 회원탈퇴 API에 대한 설명입니다.")
public class UserController {
	private final UserService userService;

	@Operation(summary = "[DELETE] 회원탈퇴")
	@DeleteMapping("/signout")
	public Mono<ResponseEntity<LogoutResponse>> deleteUser(
		@AuthenticationPrincipal CustomOAuth2User customOAuth2User, @RequestBody WithdrawalRequest request) {
		return userService.deleteUser(customOAuth2User.getUserId(), request)
			.then(Mono.fromCallable(() -> ResponseEntity.ok().body(new LogoutResponse(200, "회원탈퇴에 성공하였습니다."))));
	}
}
