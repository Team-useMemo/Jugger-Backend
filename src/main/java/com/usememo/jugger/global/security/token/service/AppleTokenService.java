package com.usememo.jugger.global.security.token.service;

import com.nimbusds.jwt.SignedJWT;
import com.usememo.jugger.global.exception.BaseException;
import com.usememo.jugger.global.exception.ErrorCode;
import com.usememo.jugger.global.security.AppleJwtGenerator;
import com.usememo.jugger.global.security.token.domain.AppleProperties;
import com.usememo.jugger.global.security.token.domain.AppleUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppleTokenService {
    private final WebClient webClient;
    private final AppleJwtGenerator appleJwtGenerator;
    private final AppleProperties appleProperties;

    // 1. 인가 코드로 Apple 토큰 요청
    public Mono<Map<String, Object>> exchangeCodeForTokens(String code) {
        return Mono.fromCallable(() -> appleJwtGenerator.createClientSecret())
                .flatMap(clientSecret ->
                        webClient.post()
                                .uri("https://appleid.apple.com/auth/token")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                                        .with("code", code)
                                        .with("redirect_uri", appleProperties.getRedirectUri())
                                        .with("client_id", appleProperties.getClientId())
                                        .with("client_secret", clientSecret))
                                .retrieve()
                                .onStatus(status -> !status.is2xxSuccessful(),
                                        response -> Mono.error(new BaseException(ErrorCode.APPLE_TOKEN_REQUEST_FAILED)))
                                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                                })
                )
                .onErrorResume(e -> Mono.error(new BaseException(ErrorCode.APPLE_CLIENT_SECRET_FAILED)));
    }

    // 2. id_token 검증 및 사용자 정보 추출
    public Mono<AppleUserResponse> extractUserFromIdToken(Map<String, Object> tokenMap) {
        return Mono.fromCallable(() -> {
            String idToken = (String) tokenMap.get("id_token");
            SignedJWT jwt = SignedJWT.parse(idToken);
            var claims = jwt.getJWTClaimsSet();

            String email = claims.getStringClaim("email");
            String sub = claims.getSubject();

            if (sub == null || email == null) {
                throw new BaseException(ErrorCode.APPLE_USERINFO_MISSING);
            }

            return new AppleUserResponse(sub, email);
        }).onErrorResume(e -> Mono.error(new BaseException(ErrorCode.APPLE_TOKEN_PARSE_ERROR)));
    }
}
