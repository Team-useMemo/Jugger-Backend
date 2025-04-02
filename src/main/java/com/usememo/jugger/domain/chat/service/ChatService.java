package com.usememo.jugger.domain.chat.service;

import com.usememo.jugger.domain.chat.dto.PostChatDto;

import reactor.core.publisher.Mono;

public interface ChatService {

	Mono<Void> processChat(PostChatDto postChatDto);
}
