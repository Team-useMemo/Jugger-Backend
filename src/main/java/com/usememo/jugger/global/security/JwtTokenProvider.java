package com.usememo.jugger.global.security;

import static io.jsonwebtoken.security.Keys.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import javax.crypto.SecretKey;


@Component
public class JwtTokenProvider {
	@Value("${spring.jwt.secret}")
	private String SECRET_KEY;

	@Value("${spring.jwt.access-token-duration}")
	private Duration accessTokenDuration;
	private final MacAlgorithm alg = Jwts.SIG.HS512;

	public String createAccessToken(UUID userId) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + accessTokenDuration.toMillis() );


		SecretKey key = hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

		return Jwts.builder()
			.claims(Map.of("sub",userId.toString() ))
			.issuedAt(now)
			.expiration(expiryDate)
			.signWith(key, alg)
			.compact();
	}

	public String createRefreshToken(UUID userId, long validityMillis){
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + validityMillis);
		SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

		return Jwts.builder()
			.claims(Map.of("sub",userId.toString()))
			.issuedAt(now)
			.expiration(expiryDate)
			.signWith(key,alg)
			.compact();
	}

	public String getUserIdFromToken(String token){

		return Jwts.parser()
			.verifyWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.getSubject();
	}


	public boolean validateToken(String token) {
		try {
			Jwts.parser()
				.verifyWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
				.build()
				.parseSignedClaims(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
