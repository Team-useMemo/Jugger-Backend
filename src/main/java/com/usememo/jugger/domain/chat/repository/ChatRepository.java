package com.usememo.jugger.domain.chat.repository;

import java.time.ZonedDateTime;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.usememo.jugger.domain.chat.entity.Chat;

import reactor.core.publisher.Flux;

public interface ChatRepository extends ReactiveMongoRepository<Chat, String> {
	Flux<Chat> findByCreatedAtBeforeOrderByCreatedAtDesc(ZonedDateTime before);
}
