package com.usememo.jugger.global.security.token.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usememo.jugger.domain.user.entity.User;
import com.usememo.jugger.domain.user.repository.UserRepository;
import com.usememo.jugger.global.exception.BaseException;
import com.usememo.jugger.global.exception.ErrorCode;
import com.usememo.jugger.global.security.JwtTokenProvider;
import com.usememo.jugger.global.security.token.domain.oAuthProperties.NaverOAuthProperties;
import com.usememo.jugger.global.security.token.domain.signUp.NaverSignUpRequest;
import com.usememo.jugger.global.security.token.domain.token.NewTokenResponse;
import com.usememo.jugger.global.security.token.domain.token.TokenResponse;
import com.usememo.jugger.global.security.token.domain.userResponse.NaverUserResponse;
import com.usememo.jugger.global.security.token.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverOAuthService implements OAuthService {
    private final WebClient webClient = WebClient.create();
    private final NaverOAuthProperties naverProps;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public Mono<TokenResponse> loginWithNaver(String code) {
        return getAccessToken(code)
                .flatMap(this::getUserInfo)
                .flatMap(this::saveOrFindUser)
                .flatMap(user -> {
                            return jwtTokenProvider.createTokenBundle(user.getUuid(),user.getEmail());
                        }
                );
    }

    private Mono<String> getAccessToken(String code) {

        return webClient.post()
                .uri("https://nid.naver.com/oauth2.0/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("client_id", naverProps.getClientId())
                        .with("client_secret", naverProps.getClientSecret())
                        .with("redirect_uri", naverProps.getRedirectUri())
                        .with("code", code))
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), response -> Mono.error(new BaseException(ErrorCode.NAVER_TOKEN_REQUEST_FAILED)))
                .bodyToMono(Map.class)
                .map(body -> {
                    String token = (String) body.get("access_token");
                    if (token == null) {
                        throw new BaseException(ErrorCode.NAVER_TOKEN_MISSING);
                    }
                    return token;
                })
                .onErrorMap(e -> new BaseException(ErrorCode.NAVER_CONNECTION_FAILED));
    }

    private Mono<NaverUserResponse> getUserInfo(String accessToken) {
        return webClient.get()
                .uri("https://openapi.naver.com/v1/nid/me")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(raw -> {
                    try {
                        NaverUserResponse parsed = new ObjectMapper().readValue(raw, NaverUserResponse.class);
                        return Mono.just(parsed);
                    } catch (Exception e) {
                        return Mono.error(new BaseException(ErrorCode.NAVER_JSON_PARSE_ERROR));
                    }
                });
    }

    private Mono<User> saveOrFindUser(NaverUserResponse response) {
        String email = response.getResponse().getEmail();
        String name = response.getResponse().getNickname();

        if (email == null) {
            return Mono.error(new BaseException(ErrorCode.NAVER_EMAIL_MISSING));
        }
        if (name == null) {
            return Mono.error(new BaseException(ErrorCode.NAVER_NAME_MISSING));
        }

        return userRepository.findByEmailAndDomain(email, "naver")
                .switchIfEmpty(Mono.defer(() -> {
                    return Mono.error(new BaseException(ErrorCode.NAVER_USER_NOT_FOUND));
                }));
    }

    public Mono<TokenResponse> signUpNaver(NaverSignUpRequest naverSignUpRequest) {
        String email = naverSignUpRequest.email();
        String domain = naverSignUpRequest.domain();
        String name = naverSignUpRequest.name();

        if (!naverSignUpRequest.terms().isTermsOfService() || !naverSignUpRequest.terms().isPrivacyPolicy()) {
            return Mono.error(new BaseException(ErrorCode.REQUIRED_TERMS_NOT_AGREED));
        }


        return userRepository.findByEmailAndDomainAndName(email, domain, name)
                .flatMap(existingUser ->
                        Mono.<TokenResponse>error(new BaseException(ErrorCode.DUPLICATE_USER))
                )
                .switchIfEmpty(Mono.defer(() -> {
                    String uuid = UUID.randomUUID().toString();

                    User.Terms terms = new User.Terms();
                    terms.setMarketing(naverSignUpRequest.terms().isMarketing());
                    terms.setPrivacyPolicy(naverSignUpRequest.terms().isPrivacyPolicy());
                    terms.setTermsOfService(naverSignUpRequest.terms().isTermsOfService());

                    User user = User.builder()
                            .uuid(uuid)
                            .name(name)
                            .email(email)
                            .domain(domain)
                            .terms(terms)
                            .build();

                    return userRepository.save(user)
                            .flatMap(savedUser -> jwtTokenProvider.createTokenBundle(savedUser.getUuid(),
                                savedUser.getEmail()));
                }));
    }

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

    public Mono<Void> deleteUser(String userId) {
        return refreshTokenRepository.deleteByUserId(userId)
                .then(userRepository.deleteById(userId))
                .onErrorResume(e -> Mono.error(new BaseException(ErrorCode.NAVER_USER_NOT_FOUND)));
    }
}
