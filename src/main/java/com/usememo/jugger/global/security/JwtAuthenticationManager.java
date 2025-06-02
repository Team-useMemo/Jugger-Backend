package com.usememo.jugger.global.security;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

	private final JwtTokenProvider jwtTokenProvider;

	@Value("${master.id}")
	private String masterUuid;

	@Value("${master.token}")
	private String masterToken;

	public JwtAuthenticationManager(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	public Mono<Authentication> authenticate(Authentication authentication) {
		String token = authentication.getCredentials().toString();
		if (isMasterToken(token)) {
			CustomOAuth2User principal = new CustomOAuth2User(
				Map.of("userId", masterUuid), masterUuid
			);
			Authentication auth = new UsernamePasswordAuthenticationToken(principal, token, List.of());
			return Mono.just(auth);
		}

		return jwtTokenProvider.getAuthentication(token);
	}

	private boolean isMasterToken(String token){
		return token.equals(masterToken);
	}
}