package com.usememo.jugger.domain.chat.repository;

import java.time.Instant;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.usememo.jugger.domain.chat.entity.Chat;

import reactor.core.publisher.Flux;

public interface ChatRepository extends ReactiveMongoRepository<Chat, String> {
	Flux<Chat> findByCreatedAtBeforeOrderByCreatedAtDesc(Instant before);

	Flux<Chat> findByCreatedAtAfterOrderByCreatedAtDesc(Instant after);

	Flux<Chat> findByCategoryUuidAndCreatedAtBeforeOrderByCreatedAtDesc(String categoryId, Instant before);

	Flux<Chat> findByCategoryUuidAndCreatedAtAfterOrderByCreatedAtDesc(String categoryId, Instant after);

}
