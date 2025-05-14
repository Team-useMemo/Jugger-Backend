package com.usememo.jugger.global.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.usememo.jugger.global.exception.BaseException;
import com.usememo.jugger.global.exception.ErrorCode;
import com.usememo.jugger.global.security.token.domain.RefreshToken;
import com.usememo.jugger.global.security.token.domain.TokenResponse;
import com.usememo.jugger.global.security.token.repository.RefreshTokenRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

	@Value("${spring.jwt.secret}")
	private String secret;

	@Value("${spring.jwt.access-token-duration}")
	private Duration accessTokenDuration;

	@Value("${spring.jwt.refresh-token-duration}")
	private Duration refreshTokenDuration;

	private final MacAlgorithm alg = Jwts.SIG.HS512;
	private SecretKey key;
	private final RefreshTokenRepository refreshTokenRepository;

	@PostConstruct
	public void init() {
		try {
			this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			throw new BaseException(ErrorCode.JWT_KEY_GENERATION_FAILED);
		}
	}

	public String createAccessToken(String userId) {
		try {
			Date now = new Date();
			Date expiry = new Date(now.getTime() + accessTokenDuration.toMillis());

			String token = Jwts.builder()
				.claims(Map.of("sub", userId))
				.issuedAt(now)
				.expiration(expiry)
				.signWith(key, alg)
				.compact();
			return token;
		} catch (Exception e) {
			throw new BaseException(ErrorCode.JWT_ACCESS_TOKEN_CREATION_FAILED);
		}
	}

	public String createRefreshToken(String userId) {
		try {
			Date now = new Date();
			Date expiry = new Date(now.getTime() + refreshTokenDuration.toMillis());

			String token = Jwts.builder()
				.claims(Map.of("sub", userId))
				.issuedAt(now)
				.expiration(expiry)
				.signWith(key, alg)
				.compact();
			return token;
		} catch (Exception e) {
			throw new BaseException(ErrorCode.JWT_REFRESH_TOKEN_CREATION_FAILED);
		}
	}

	public String getUserIdFromToken(String token) {
		try {
			return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.getSubject();
		} catch (JwtException e) {
			throw new BaseException(ErrorCode.JWT_PARSE_FAILED);
		}
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public Mono<TokenResponse> createTokenBundle(String userId) {
		try {
			String accessToken = createAccessToken(userId);
			String refreshToken = createRefreshToken(userId);

			Instant expiryDate = Instant.now().plus(refreshTokenDuration);

			RefreshToken refreshTokenEntity = RefreshToken.builder()
				.userId(userId)
				.token(refreshToken)
				.expiryDate(expiryDate)
				.build();

			return refreshTokenRepository.save(refreshTokenEntity)
				.thenReturn(new TokenResponse(accessToken, refreshToken));
		} catch (Exception e) {
			return Mono.error(new BaseException(ErrorCode.JWT_BUNDLE_CREATION_FAILED));
		}
	}


	public Mono<Authentication> getAuthentication(String token) {
		try {
			Claims claims = Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();

			String userId = claims.getSubject();
			Map<String, Object> attributes = Map.of("userId", userId);
			CustomOAuth2User principal = new CustomOAuth2User(attributes, userId);
			Authentication auth = new UsernamePasswordAuthenticationToken(principal, token, List.of());
			return Mono.just(auth);
		} catch (Exception e) {
			return Mono.empty();
		}
	}
}
