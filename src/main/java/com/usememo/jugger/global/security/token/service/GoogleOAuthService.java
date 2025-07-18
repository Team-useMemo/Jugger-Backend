package com.usememo.jugger.global.security.token.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;


import com.usememo.jugger.global.exception.BaseException;
import com.usememo.jugger.global.exception.ErrorCode;

import com.usememo.jugger.global.security.JwtTokenProvider;
import com.usememo.jugger.global.security.token.domain.token.TokenResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor

public class GoogleOAuthService {
	private final WebClient webClient = WebClient.create();
	private final JwtTokenProvider jwtTokenProvider;
	private final SignService signService;


	@Value("${google.client-id}")
	private String clientId;

	@Value("${google.client-secret}")
	private String clientSecret;

	@Value("${google.redirect-uri}")
	private String redirectUri;


	private final String domain = "google";

	public Mono<TokenResponse> loginWithGoogle(String code) {
		return getAccessToken(code)
			.flatMap(this::getUserInfo)
			.flatMap(stringObjectMap -> signService.saveOrFindUser(stringObjectMap,domain))
			.flatMap(user -> jwtTokenProvider.createTokenBundle(user.getUuid()));
	}


    private Mono<String> getAccessToken(String code) {
        String decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8);

		return webClient.post()
			.uri("https://oauth2.googleapis.com/token")
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(BodyInserters.fromFormData("grant_type", "authorization_code")
				.with("client_id", clientId)
				.with("client_secret", clientSecret)
				.with("redirect_uri", redirectUri)
				.with("code", decodedCode))
			.retrieve()
			.bodyToMono(Map.class)
			.map(body -> {
				String token = (String)body.get("access_token");
				if (token == null) {
					throw new BaseException(ErrorCode.GOOGLE_LOGIN_FAIL);
				}
				return token;
			})
			.onErrorMap(e -> new BaseException(ErrorCode.GOOGLE_LOGIN_FAIL));
	}

	private Mono<Map<String, Object>> getUserInfo(String accessToken) {
		return webClient.get()
			.uri("https://www.googleapis.com/oauth2/v3/userinfo")
			.headers(headers -> headers.setBearerAuth(accessToken))
			.retrieve()
			.bodyToMono(new ParameterizedTypeReference<>() {
			});
	}



}
