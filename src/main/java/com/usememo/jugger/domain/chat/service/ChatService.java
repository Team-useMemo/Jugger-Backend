package com.usememo.jugger.domain.chat.service;

import java.time.Instant;
import java.util.List;

import com.usememo.jugger.domain.chat.dto.GetChatByCategoryDto;
import com.usememo.jugger.domain.chat.dto.PostChatDto;

import reactor.core.publisher.Mono;

public interface ChatService {

	// TODO: V2 AI 서버 연동시
	Mono<Void> processChat(PostChatDto postChatDto);

	Mono<Void> postChat(PostChatDto postChatDto);

	Mono<List<GetChatByCategoryDto>> getChatsBefore(Instant before, int page, int size);

	Mono<List<GetChatByCategoryDto>> getChatsAfter(Instant before, int page, int size);

	Mono<List<GetChatByCategoryDto>> getChatsByCategoryIdBefore(String categoryId, Instant before, int page, int size);

	Mono<List<GetChatByCategoryDto>> getChatsByCategoryIdAfter(String categoryId, Instant after, int page, int size);

}
