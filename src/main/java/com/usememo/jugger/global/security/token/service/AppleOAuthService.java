package com.usememo.jugger.global.security.token.service;

import com.usememo.jugger.domain.user.entity.User;
import com.usememo.jugger.domain.user.repository.UserRepository;
import com.usememo.jugger.global.exception.BaseException;
import com.usememo.jugger.global.exception.ErrorCode;
import com.usememo.jugger.global.security.JwtTokenProvider;
import com.usememo.jugger.global.security.token.domain.AppleSignUpRequest;
import com.usememo.jugger.global.security.token.domain.AppleUserResponse;
import com.usememo.jugger.global.security.token.domain.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppleOAuthService {
    private final AppleTokenService appleTokenService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public Mono<TokenResponse> loginWithApple(String code) {
        return appleTokenService.exchangeCodeForTokens(code)
                .flatMap(appleTokenService::extractUserFromIdToken)
                .flatMap(this::findUser)
                .flatMap(user -> jwtTokenProvider.createTokenBundle(user.getUuid()));
    }

    private Mono<User> findUser(AppleUserResponse userInfo) {
        return userRepository.findByEmailAndDomain(userInfo.email(), "apple")
                .switchIfEmpty(Mono.error(new BaseException(ErrorCode.APPLE_USER_NOT_FOUND)));
    }

    public Mono<TokenResponse> signUpApple(AppleSignUpRequest request) {
        String email = request.email();
        String name = request.name();

        return userRepository.findByEmailAndDomainAndName(email, "apple", name)
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
                            .domain("apple")
                            .build();

                    return userRepository.save(user)
                            .flatMap(savedUser -> jwtTokenProvider.createTokenBundle(savedUser.getUuid()));
                }));
    }
}