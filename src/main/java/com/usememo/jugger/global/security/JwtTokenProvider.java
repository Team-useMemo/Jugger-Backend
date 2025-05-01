package com.usememo.jugger.global.security;

import static io.jsonwebtoken.security.Keys.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;

import com.usememo.jugger.global.exception.BaseException;
import com.usememo.jugger.global.exception.ErrorCode;
import com.usememo.jugger.global.security.token.domain.TokenResponse;
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

	@PostConstruct
	public void init() {
		try {
			log.info("🔐 JWT 키 초기화 시작");
			this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
			log.info("✅ JWT 키 초기화 완료");
		} catch (Exception e) {
			log.error("❌ JWT 키 생성 실패", e);
			throw new BaseException(ErrorCode.JWT_KEY_GENERATION_FAILED);
		}
	}

	public String createAccessToken(String userId) {
		try {
			log.info("🔐 accessToken 생성 시도 - userId: {}", userId);
			Date now = new Date();
			Date expiry = new Date(now.getTime() + accessTokenDuration.toMillis());

			String token = Jwts.builder()
				.claims(Map.of("sub", userId))
				.issuedAt(now)
				.expiration(expiry)
				.signWith(key, alg)
				.compact();

			log.info("✅ accessToken 생성 완료");
			return token;
		} catch (Exception e) {
			log.error("❌ accessToken 생성 실패", e);
			throw new BaseException(ErrorCode.JWT_ACCESS_TOKEN_CREATION_FAILED);
		}
	}

	public String createRefreshToken(String userId) {
		try {
			log.info("🔄 refreshToken 생성 시도 - userId: {}", userId);
			Date now = new Date();
			Date expiry = new Date(now.getTime() + refreshTokenDuration.toMillis());

			String token = Jwts.builder()
				.claims(Map.of("sub", userId))
				.issuedAt(now)
				.expiration(expiry)
				.signWith(key, alg)
				.compact();

			log.info("✅ refreshToken 생성 완료");
			return token;
		} catch (Exception e) {
			log.error("❌ refreshToken 생성 실패", e);
			throw new BaseException(ErrorCode.JWT_REFRESH_TOKEN_CREATION_FAILED);
		}
	}

	public String getUserIdFromToken(String token) {
		try {
			log.info("🔍 토큰에서 userId 추출 시도");
			String userId = Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.getSubject();
			log.info("✅ 추출된 userId: {}", userId);
			return userId;
		} catch (JwtException e) {
			log.error("❌ 토큰 파싱 실패", e);
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
			log.warn("⚠️ 토큰 검증 실패", e);
			return false;
		}
	}

	public TokenResponse createTokenBundle(String userId) {
		log.info("📦 토큰 번들 생성 시도 - userId: {}", userId);
		try {
			String accessToken = createAccessToken(userId);
			String refreshToken = createRefreshToken(userId);
			log.info("✅ 토큰 번들 생성 완료");
			return new TokenResponse(accessToken, refreshToken);
		} catch (Exception e) {
			log.error("❌ 토큰 번들 생성 실패", e);
			throw new BaseException(ErrorCode.JWT_BUNDLE_CREATION_FAILED);
		}
	}
}
