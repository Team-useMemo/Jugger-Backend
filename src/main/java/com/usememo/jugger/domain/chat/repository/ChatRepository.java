package com.usememo.jugger.domain.chat.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.usememo.jugger.domain.chat.entity.Chat;

public interface ChatRepository extends ReactiveMongoRepository<Chat, String> {
}
