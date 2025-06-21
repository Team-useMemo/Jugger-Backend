package com.usememo.jugger.domain.chat.repository;

import java.time.Instant;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.usememo.jugger.domain.chat.entity.Chat;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatRepository extends ReactiveMongoRepository<Chat, String> {
	Flux<Chat> findByUserUuidAndCreatedAtBeforeOrderByCreatedAtDesc(String userUuid, Instant before);

	Flux<Chat> findByUserUuidAndCreatedAtAfterOrderByCreatedAtDesc(String userUuid, Instant after);

	Flux<Chat> findByCategoryUuidAndCreatedAtBeforeOrderByCreatedAtDesc(String categoryId, Instant before);

	Flux<Chat> findByCategoryUuidAndCreatedAtAfterOrderByCreatedAtDesc(String categoryId, Instant after);

	Mono<Chat> findFirstByCategoryUuidOrderByCreatedAtDesc(String categoryUuid);

	Mono<Void> deleteByCategoryUuid(String categoryUuid);

}
