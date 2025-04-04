package com.usememo.jugger.domain.chat.service;

import com.usememo.jugger.domain.chat.dto.PostChatDto;

import reactor.core.publisher.Mono;

public interface ChatService {

	// TODO: V2 AI 서버 연동시
	Mono<Void> processChat(PostChatDto postChatDto);

	Mono<Void> postChat(PostChatDto postChatDto);

}
