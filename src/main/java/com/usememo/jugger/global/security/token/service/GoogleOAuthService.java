package com.usememo.jugger.global.security.token.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.usememo.jugger.domain.user.entity.User;
import com.usememo.jugger.domain.user.repository.UserRepository;
import com.usememo.jugger.global.exception.BaseException;
import com.usememo.jugger.global.exception.ErrorCode;
import com.usememo.jugger.global.exception.KakaoException;
import com.usememo.jugger.global.security.JwtTokenProvider;

import com.usememo.jugger.global.security.token.domain.GoogleSignupRequest;
import com.usememo.jugger.global.security.token.domain.NewTokenResponse;
import com.usememo.jugger.global.security.token.domain.TokenResponse;
import com.usememo.jugger.global.security.token.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOAuthService implements OAuthService {
    private final WebClient webClient = WebClient.create();
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    public Mono<TokenResponse> loginWithGoogle(String code) {
        return getAccessToken(code)
                .flatMap(this::getUserInfo)
                .flatMap(this::saveOrFindUser)
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
                    String token = (String) body.get("access_token");
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

    private Mono<User> saveOrFindUser(Map<String, Object> userInfo) {
        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        if (email == null) {
            return Mono.error(new BaseException(ErrorCode.GOOGLE_NO_EMAIL));
        }
        if (name == null) {
            return Mono.error(new BaseException(ErrorCode.GOOGLE_NO_NAME));
        }

        return userRepository.findByEmailAndDomain(email, "google")
                .switchIfEmpty(Mono.defer(() -> {
                    return Mono.error(new KakaoException(ErrorCode.KAKAO_USER_NOT_FOUND,
                            Map.of("email", email, "nickname", name)));
                }));
    }

    public Mono<TokenResponse> signUpGoogle(GoogleSignupRequest request) {
        String email = request.email();
        String name = request.name();

        return userRepository.findByEmailAndDomainAndName(email, "google", name)
                .flatMap(existingUser -> Mono.<TokenResponse>error(new BaseException(ErrorCode.DUPLICATE_USER)))
                .switchIfEmpty(Mono.defer(() -> {
                    String uuid = UUID.randomUUID().toString();

                    User.Terms terms = new User.Terms();
                    terms.setMarketing(request.terms().isMarketing());
                    terms.setPrivacyPolicy(request.terms().isPrivacyPolicy());
                    terms.setTermsOfService(request.terms().isTermsOfService());

                    User user = User.builder()
                            .uuid(uuid)
                            .name(name)
                            .email(email)
                            .terms(terms)
                            .domain("google")
                            .build();

                    return userRepository.save(user)
                            .flatMap(savedUser -> jwtTokenProvider.createTokenBundle(savedUser.getUuid()));
                }));
    }

    @Override
    public Mono<Void> userLogOut(String refreshToken) {
        String userId;
        userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        if (userId == null) {
            throw new BaseException(ErrorCode.NO_LOGOUT_USER);
        }
        return refreshTokenRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new BaseException(ErrorCode.NO_LOGOUT_USER)))
                .flatMap(foundToken -> refreshTokenRepository.deleteByUserId(userId));
    }


    @Override
    public Mono<ResponseEntity<NewTokenResponse>> giveNewToken(String refreshToken) {
        return refreshTokenRepository.findByToken(refreshToken)
                .flatMap(savedToken -> {
                    if (!jwtTokenProvider.validateToken(savedToken.getToken())) {
                        return Mono.error(new BaseException(ErrorCode.NO_REFRESH_TOKEN));
                    }
                    String userId = jwtTokenProvider.getUserIdFromToken(savedToken.getToken());
                    return userRepository.findById(userId)
                            .map(user -> {
                                String newAccessToken = jwtTokenProvider.createAccessToken(userId);
                                return ResponseEntity.ok().body(new NewTokenResponse(newAccessToken));
                            });
                })
                .switchIfEmpty(Mono.error(new BaseException(ErrorCode.NO_REFRESH_TOKEN)));
    }

}
