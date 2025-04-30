package com.usememo.jugger.global.security;

import static io.jsonwebtoken.security.Keys.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import javax.crypto.SecretKey;

import com.usememo.jugger.global.security.token.domain.TokenResponse;

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
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public String createAccessToken(String userId) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + accessTokenDuration.toMillis());

		return Jwts.builder()
			.claims(Map.of("sub",userId))
			.issuedAt(now)
			.expiration(expiry)
			.signWith(key, alg)
			.compact();
	}

	public String createRefreshToken(String userId) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + refreshTokenDuration.toMillis());


		return Jwts.builder()
			.claims(Map.of("sub",userId))
			.issuedAt(now)
			.expiration(expiry)
			.signWith(key, alg)
			.compact();
	}

	public String getUserIdFromToken(String token) {
		return Jwts.parser()
			.verifyWith(key)
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.getSubject();
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

	public TokenResponse createTokenBundle(String userId) {
		return new TokenResponse(createAccessToken(userId), createRefreshToken(userId));
	}
}
