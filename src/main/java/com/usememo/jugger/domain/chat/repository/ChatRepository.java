package com.usememo.jugger.domain.chat.repository;

import java.time.Instant;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.usememo.jugger.domain.chat.entity.Chat;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatRepository extends ReactiveMongoRepository<Chat, String> {
	Flux<Chat> findByUserUuidAndCreatedAtBeforeOrderByCreatedAtDesc(Instant before, String userUuid);

	Flux<Chat> findByUserUuidAndCreatedAtAfterOrderByCreatedAtDesc(Instant after, String userUuid);

	Flux<Chat> findByCategoryUuidAndCreatedAtBeforeOrderByCreatedAtDesc(String categoryId, Instant before);

	Flux<Chat> findByCategoryUuidAndCreatedAtAfterOrderByCreatedAtDesc(String categoryId, Instant after);

	Mono<Chat> findFirstByCategoryUuidOrderByCreatedAtDesc(String categoryUuid);

}
