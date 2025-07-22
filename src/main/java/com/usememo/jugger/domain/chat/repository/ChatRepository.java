package com.usememo.jugger.domain.chat.repository;

import java.time.Instant;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.usememo.jugger.domain.chat.entity.Chat;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatRepository extends ReactiveMongoRepository<Chat, String> {
	Flux<Chat> findByUserUuidAndCreatedAtBeforeOrderByCreatedAtDesc(String userUuid, Instant before);

	Flux<Chat> findByUserUuidAndCreatedAtAfterOrderByCreatedAtDesc(String userUuid, Instant after);

	Flux<Chat> findByCategoryUuidAndUserUuidAndCreatedAtBeforeOrderByCreatedAtDesc(String userId,String categoryId, Instant before);

	Flux<Chat> findByCategoryUuidAndUserUuidAndCreatedAtAfterOrderByCreatedAtDesc(String userId, String categoryId, Instant after);

	Mono<Chat> findFirstByCategoryUuidAndUserUuidOrderByCreatedAtDesc(String userId,String categoryUuid);

	Mono<Void> deleteByCategoryUuid(String categoryUuid);

	Mono<Void> deleteByUserUuid(String userId);

	Mono<Chat> findByUuidAndUserUuid(String chatId, String userId);

	Mono<Void> deleteByUuidAndUserUuid(String chatId,String userId);

	Mono<Chat> findByRefs_CalendarUuid(String calendarUuid);

}
