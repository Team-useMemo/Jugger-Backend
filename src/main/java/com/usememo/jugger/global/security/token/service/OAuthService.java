package com.usememo.jugger.global.security.token.service;

import com.usememo.jugger.global.security.token.domain.NewTokenResponse;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface OAuthService {
    Mono<ResponseEntity<NewTokenResponse>> giveNewToken(String refreshToken);
    Mono<Void> userLogOut(String refreshToken);
}
