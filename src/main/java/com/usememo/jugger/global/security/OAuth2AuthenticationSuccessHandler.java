package com.usememo.jugger.global.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.NonNull;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import com.usememo.jugger.global.security.token.domain.RefreshToken;
import com.usememo.jugger.global.security.token.repository.RefreshTokenRepository;

@Component
public class OAuth2AuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {

	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenRepository refreshTokenRepository;

	@Value("${spring.jwt.refresh-token-duration}")
	private  Duration REFRESH_TOKEN_DURATION;

	public OAuth2AuthenticationSuccessHandler(JwtTokenProvider jwtTokenProvider,RefreshTokenRepository refreshTokenRepository) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.refreshTokenRepository = refreshTokenRepository;
	}

	@Override
	public Mono<Void> onAuthenticationSuccess(@NonNull WebFilterExchange webFilterExchange, @NonNull Authentication authentication) {
		ServerWebExchange exchange = webFilterExchange.getExchange();

		CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
		String userId = oAuth2User.getUserId();

		String accessToken = jwtTokenProvider.createAccessToken(userId);

		String refreshToken = jwtTokenProvider.createRefreshToken(userId);

		RefreshToken tokenDoc = RefreshToken.builder()
			.userId(userId)
			.token(refreshToken)
			.expiryDate(Instant.now().plus(REFRESH_TOKEN_DURATION))
			.build();
		ResponseCookie cookie = ResponseCookie.from("refresh_token",refreshToken)
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(REFRESH_TOKEN_DURATION)
			.sameSite("Strict")
			.build();



		byte[] body = ("{\"accessToken\":\"" + accessToken + "\"}").getBytes(StandardCharsets.UTF_8);

		exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
		exchange.getResponse().getCookies().add("refresh_token",cookie);
		exchange.getResponse().setStatusCode(HttpStatus.OK);

		return refreshTokenRepository.save(tokenDoc)
			.then(exchange.getResponse().writeWith(
				Mono.just(exchange.getResponse().bufferFactory().wrap(body))
			));
	}
}
