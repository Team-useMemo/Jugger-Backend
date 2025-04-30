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
			return Mono.just(ResponseEntity.noContent().build());
		}

		String userId;
		try {
			userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
		} catch (Exception e) {
			return Mono.just(ResponseEntity.noContent().build());
		}
		//프론트에서 인가 코드를 받아서 -> 코드를 서버가 가져서 카카오에 요청을 받아서 처리하는 것이다.
		//사용자 정보를 저장하는 형식인 것이다.
		//프론트에 jwt 토큰을 넘겨주면 되는 것이다.
		//redirection 때문에 문제가 발생하는 것이다.
		//security 설정해서 이대로 쓰기
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

	// @GetMapping("/success")
	// public Mono<ResponseEntity<Map<String, String>>> loginSuccess(Authentication authentication) {
	// 	CustomOAuth2User user = (CustomOAuth2User) authentication.getPrincipal();
	// 	String accessToken = jwtTokenProvider.createAccessToken(UUID.fromString(user.getUserId()));
	// 	return Mono.just(ResponseEntity.ok(Map.of("accessToken", accessToken)));
	// }

}
