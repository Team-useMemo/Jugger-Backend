package com.usememo.jugger.global.security.token.repository;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.usememo.jugger.global.security.token.domain.RefreshToken;

import reactor.core.publisher.Mono;

public interface RefreshTokenRepository extends ReactiveCrudRepository<RefreshToken,String> {
	Mono<RefreshToken> findByToken(String token);
	Mono<Void> deleteByUserId(UUID UserId);
}
