package com.usememo.jugger.global.security.token.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.usememo.jugger.domain.user.repository.UserRepository;
import com.usememo.jugger.global.security.CustomOAuth2User;
import com.usememo.jugger.global.security.JwtTokenProvider;
import com.usememo.jugger.global.security.token.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
	private final RefreshTokenRepository refreshTokenRepository;
	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;

	@PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> refreshAccessToken(@CookieValue(name = "refresh_token",required = false) String refreshToken){
		if(refreshToken == null){
			return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("리프레시 토큰이 없습니다"));
		}
		return refreshTokenRepository.findByToken(refreshToken)
			.flatMap(savedToken->{
				if(!jwtTokenProvider.validateToken(savedToken.getToken())){
					return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("만료된 리프레시 토큰입니다."));
				}
					String userId = jwtTokenProvider.getUserIdFromToken(savedToken.getToken());
					return userRepository.findById(userId)
						.map(user->{
							String newAccessToken = jwtTokenProvider.createAccessToken(UUID.fromString(userId));
							return ResponseEntity.ok().body("{\"accessToken\":\""+newAccessToken+"\"}");
						});
			})
			.switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("존재하지 않는 리프레시 토큰입니다.")));
	}

	@PostMapping("/logout")
	public Mono<ResponseEntity<Void>> logout(@CookieValue(name = "refresh_token", required = false) String refreshToken) {
		if (refreshToken == null) {
			return Mono.just(ResponseEntity.noContent().build()); // 쿠키 없으면 무시
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
					.sameSite("None")
					.maxAge(0)
					.build()
					.toString()
				)
				.build());
	}

	@GetMapping("/success")
	public Mono<ResponseEntity<Map<String, String>>> loginSuccess(Authentication authentication) {
		CustomOAuth2User user = (CustomOAuth2User) authentication.getPrincipal();
		String accessToken = jwtTokenProvider.createAccessToken(UUID.fromString(user.getUserId()));
		return Mono.just(ResponseEntity.ok(Map.of("accessToken", accessToken)));
	}

}
